package com.socrata.regioncoder

import java.util.concurrent.Executors
import javax.servlet.http.HttpServletResponse
import com.socrata.geospace.lib.errors.ServiceDiscoveryException
import com.socrata.http.client.{HttpClient, HttpClientHttpClient, NoopLivenessChecker}
import com.socrata.http.server.{HttpRequest, HttpResponse, SocrataServerJetty}
import com.socrata.http.server.responses._
import com.socrata.http.server.implicits._
import com.socrata.http.server.util.RequestId
import com.socrata.http.common.AuxiliaryData
import com.socrata.regioncoder._
import com.socrata.regioncoder.config.RegionCoderConfig
import com.socrata.soda.external.SodaFountainClient
import com.socrata.curator.ServerProvider.RetryOnAllExceptionsDuringInitialRequest
import com.socrata.curator.{CuratorFromConfig, DiscoveryFromConfig}
import com.socrata.http.server.curator.CuratorBroker
import com.typesafe.config.ConfigFactory
import com.rojoma.simplearm.v2._
import org.slf4j.{MDC, LoggerFactory}

class Main(config: RegionCoderConfig) {
  val log = LoggerFactory.getLogger(classOf[Main])

  implicit def executorResource = Resource.executorShutdownNoTimeout

  def exceptionalResult(e: Exception): HttpResponse = {
    log.error("Request errored out", e)
    InternalServerError ~> Content("text/plain", s"${e.getClass.getSimpleName}: ${e.getMessage}\n${e.getStackTraceString}\n")
  }

  def runServer() {
    for {
      httpExecutor <- managed(Executors.newCachedThreadPool())
      httpClient <- managed(new HttpClientHttpClient(
                              httpExecutor,
                              HttpClientHttpClient.defaultOptions.
                                withLivenessChecker(NoopLivenessChecker).
                                withUserAgent("region-coder")))
      curator <- CuratorFromConfig(config.curator)
      discovery <- DiscoveryFromConfig(classOf[AuxiliaryData], curator, config.discovery)
      sodaFountain <- managed(new SodaFountainClient(
                                httpClient,
                                discovery,
                                config.sodaFountain.serviceName,
                                config.curator.connectTimeout,
                                config.sodaFountain.maxRetries,
                                RetryOnAllExceptionsDuringInitialRequest,
                                throw ServiceDiscoveryException("No Soda Fountain servers found")) {
                              }).and(_.start())
    } {
      val broker = new CuratorBroker(discovery, config.discovery.address, config.discovery.name, None)

      val baseHandler = new RegionCoderServlet(config, sodaFountain)

      val wrappedHandler = { (req: HttpRequest) =>
        MDC.clear()
        MDC.put(RequestId.ReqIdHeader, req.requestId)

        log.info("{} - {}{}", req.method, req.requestPathStr, req.queryStr.fold("")("?" + _))

        val result =
          try {
            baseHandler.handle(req)
          } catch {
            case e: Exception =>
              exceptionalResult(e)
          }

        { (resp: HttpServletResponse) =>
          try {
            result(resp)
          } catch{
            case e: Exception =>
              if(!resp.isCommitted) {
                resp.reset()
                exceptionalResult(e)(resp)
              } else {
                log.warn("Caught exception but the result was already committed", e)
              }
          } finally {
            log.info("Status - " + resp.getStatus())
          }
        }
      }

      val server = new SocrataServerJetty(wrappedHandler,
                                          SocrataServerJetty.defaultOptions.
                                            withPort(config.port).
                                            withBroker(broker))
      server.run()
    }
  }
}

object Main extends App {
  new Main(new RegionCoderConfig(ConfigFactory.load().getConfig("com.socrata"))).runServer()
}
