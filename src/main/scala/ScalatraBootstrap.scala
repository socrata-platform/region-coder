import java.util.concurrent.Executors
import javax.servlet.ServletContext

import com.socrata.geospace.lib.errors.ServiceDiscoveryException
import com.socrata.http.client.{HttpClientHttpClient, NoopLivenessChecker}
import com.socrata.http.common.AuxiliaryData
import com.socrata.regioncoder._
import com.socrata.regioncoder.config.RegionCoderConfig
import com.socrata.soda.external.SodaFountainClient
import com.socrata.thirdparty.curator.ServerProvider.RetryOnAllExceptionsDuringInitialRequest
import com.socrata.thirdparty.curator.{CuratorFromConfig, DiscoveryFromConfig}
import com.typesafe.config.ConfigFactory
import org.scalatra._
import org.scalatra.metrics.MetricsSupport
import org.scalatra.metrics.MetricsSupportExtensions._

class ScalatraBootstrap extends LifeCycle with MetricsSupport {
  lazy val config = new RegionCoderConfig(ConfigFactory.load().getConfig("com.socrata"))

  lazy val curator = CuratorFromConfig.unmanaged(config.curator)
  lazy val discovery = DiscoveryFromConfig.unmanaged(classOf[AuxiliaryData], curator, config.discovery)
  lazy val httpClient = new HttpClientHttpClient(
    Executors.newCachedThreadPool(),
    HttpClientHttpClient.defaultOptions.
      withLivenessChecker(NoopLivenessChecker).
      withUserAgent("region-coder"))

  lazy val sodaFountain = new SodaFountainClient(httpClient,
    discovery,
    config.sodaFountain.serviceName,
    config.curator.connectTimeout,
    config.sodaFountain.maxRetries,
    RetryOnAllExceptionsDuringInitialRequest,
    throw ServiceDiscoveryException("No Soda Fountain servers found"))

  override def init(context: ServletContext): Unit = {
    curator.start()
    discovery.start()
    sodaFountain.start()
    context.mountMetricsAdminServlet("/metrics-admin")
    context.mountHealthCheckServlet("/health")
    context.mountMetricsServlet("/metrics")
    context.mountThreadDumpServlet("/thread-dump")
    context.installInstrumentedFilter("/v1/*")
    context.mount(new RegionCoderServlet(config, sodaFountain), "/*")
  }

  override def destroy(context: ServletContext): Unit = {
    sodaFountain.close()
    httpClient.close()
    discovery.close()
    curator.close()
  }
}
