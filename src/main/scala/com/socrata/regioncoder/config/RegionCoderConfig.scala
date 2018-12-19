package com.socrata.regioncoder.config

import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import com.socrata.curator.{DiscoveryConfig, CuratorConfig}
import com.socrata.thirdparty.metrics.MetricsOptions

class RegionCoderConfig(config: Config) {
  val port = config.getInt("region-coder.port")

  val metrics = MetricsOptions(config.getConfig("region-coder.metrics"))

  val gracefulShutdownMs = config.getMilliseconds(
    "region-coder.graceful-shutdown-time").toInt
  val shapePayloadTimeout = new FiniteDuration(config.getMilliseconds(
    "region-coder.shape-payload-timeout"), TimeUnit.MILLISECONDS)

  val cache = config.getConfig("region-coder.cache")
  val partitioning = new RegionPartitionConfig(config.getConfig("region-coder.partitioning"))

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
