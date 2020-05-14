package com.socrata.regioncoder

import java.util.concurrent.Executors
import javax.servlet.http.HttpServletResponse
import com.socrata.geospace.lib.errors.ServiceDiscoveryException
import com.socrata.http.client.{HttpClient, HttpClientHttpClient, NoopLivenessChecker}
import com.socrata.http.server.{HttpRequest, HttpResponse}
import com.socrata.http.server.responses._
import com.socrata.http.server.implicits._
import com.socrata.http.common.AuxiliaryData
import com.socrata.regioncoder._
import com.socrata.regioncoder.config.RegionCoderConfig
import com.socrata.soda.external.SodaFountainClient
import com.socrata.curator.ServerProvider.RetryOnAllExceptionsDuringInitialRequest
import com.socrata.curator.{CuratorBroker, CuratorFromConfig, DiscoveryFromConfig}
import com.typesafe.config.ConfigFactory
import com.rojoma.simplearm.v2._
import org.slf4j.{MDC, LoggerFactory}

class Main(config: RegionCoderConfig) {
  val logger = LoggerFactory.getLogger(classOf[Main])

  implicit def executorResource = Resource.executorShutdownNoTimeout

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
    } {
      val broker = new CuratorBroker(discovery, config.discovery.address, config.discovery.name, None)
      val sodaFountain = new SodaFountainClient(
        httpClient,
        discovery,
        config.sodaFountain.serviceName,
        config.curator.connectTimeout,
        config.sodaFountain.maxRetries,
        RetryOnAllExceptionsDuringInitialRequest,
        throw ServiceDiscoveryException("No Soda Fountain servers found"))

      val baseHandler = new RegionCoderServlet(config, sodaFountain)
      val reqIdHandler = { (req: HttpRequest) =>
        MDC.clear()
        val reqId: Option[String] = req.header("X-Socrata-RequestId")
        reqId.foreach(MDC.put("X-Socrata-RequestId", _ : String))
        baseHandler.handle(req)
      }
      val loggingHandler = { (req: HttpRequest) =>
        val result = try {
          reqIdHandler(req)
        } catch {
          case e: Exception =>
            logger.error("Request errored out", e)
            InternalServerError ~> Content("text/plain", s"${e.getClass.getSimpleName}: ${e.getMessage}\n${e.getStackTraceString}\n")
        }

        { (resp: HttpServletResponse) =>
          try {
            result(resp)
          } catch {
            case e: Exception =>
              logger.error("Request errored out", e)
              InternalServerError ~> Content("text/plain", s"${e.getClass.getSimpleName}: ${e.getMessage}\n${e.getStackTraceString}\n")
          } finally {
            logger.info("Status - " + resp.getStatus)
          }
        }
      }
    }
  }
}

object Main extends App {
  new Main(new RegionCoderConfig(ConfigFactory.load().getConfig("com.socrata"))).runServer()
}
