import com.socrata.geospace.lib.errors.ServiceDiscoveryException
import com.socrata.http.client.{NoopLivenessChecker, HttpClientHttpClient}
import com.socrata.http.common.AuxiliaryData
import com.socrata.regioncoder._
import com.socrata.regioncoder.config.RegionCoderConfig
import com.socrata.soda.external.SodaFountainClient
import com.socrata.thirdparty.curator.{DiscoveryFromConfig, CuratorFromConfig}
import com.socrata.thirdparty.curator.ServerProvider.RetryOnAllExceptionsDuringInitialRequest
import com.socrata.thirdparty.metrics.MetricsReporter
import com.typesafe.config.ConfigFactory
import java.util.concurrent.Executors
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  lazy val config = new RegionCoderConfig(
    ConfigFactory.load().getConfig("com.socrata.region-coder"))

  lazy val curator = CuratorFromConfig.unmanaged(config.curator)
  lazy val discovery = DiscoveryFromConfig.unmanaged(classOf[AuxiliaryData], curator, config.discovery)
  lazy val httpClient = new HttpClientHttpClient(
    Executors.newCachedThreadPool(),
    HttpClientHttpClient.defaultOptions.
      withLivenessChecker(NoopLivenessChecker).
      withUserAgent("region-coder"))

  lazy val sodaFountain =  new SodaFountainClient(httpClient,
    discovery,
    config.sodaFountain.serviceName,
    config.curator.connectTimeout,
    config.sodaFountain.maxRetries,
    RetryOnAllExceptionsDuringInitialRequest,
    throw ServiceDiscoveryException("No Soda Fountain servers found"))

  lazy val metricsReporter = new MetricsReporter(config.metrics)

  override def init(context: ServletContext): Unit = {
    curator.start()
    discovery.start()
    sodaFountain.start()
    metricsReporter
    context.mount(new RegionCoderServlet(config, sodaFountain), "/*")
  }

  override def destroy(context: ServletContext): Unit = {
    metricsReporter.stop()
    sodaFountain.close()
    httpClient.close()
    discovery.close()
    curator.close()
  }
}
