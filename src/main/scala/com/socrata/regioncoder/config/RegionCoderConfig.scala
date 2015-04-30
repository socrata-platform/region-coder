package com.socrata.regioncoder.config

import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import com.socrata.thirdparty.curator.{DiscoveryConfig, CuratorConfig}
import com.socrata.thirdparty.metrics.MetricsOptions

class RegionCoderConfig(config: Config) {
  val port = config.getInt("port")

  val metrics = MetricsOptions(config.getConfig("metrics"))

  val shapePayloadTimeout = new FiniteDuration(
    config.getMilliseconds("shape-payload-timeout"), TimeUnit.MILLISECONDS)
  val cache = config.getConfig("cache")
  val partitioning = new RegionPartitionConfig(config.getConfig("partitioning"))

  val discovery = new DiscoveryConfig(config, "service-advertisement")
  val curator = new CuratorConfig(config, "curator")
  val sodaFountain = new CuratedServiceConfig(config.getConfig("soda-fountain"))
}

class RegionPartitionConfig(config: Config) {
  val sizeX = config.getDouble("sizeX")
  val sizeY = config.getDouble("sizeY")
}

class CuratedServiceConfig(config: Config) {
  val serviceName = config.getString("service-name")
  val maxRetries  = config.getInt("max-retries")
}
