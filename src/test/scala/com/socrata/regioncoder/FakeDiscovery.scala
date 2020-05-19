package com.socrata.regioncoder

import java.util.Collection
import org.apache.curator.x.discovery.{ServiceDiscovery, ServiceProviderBuilder, ServiceProvider, ServiceInstance, ProviderStrategy, ServiceCacheBuilder, InstanceFilter, DownInstancePolicy}
import com.socrata.http.common.AuxiliaryData
import java.util.concurrent.ThreadFactory

object FakeDiscovery extends Failable with ServiceDiscovery[AuxiliaryData] with ServiceProviderBuilder[AuxiliaryData] with ServiceProvider[AuxiliaryData] {
  def serviceProviderBuilder(): ServiceProviderBuilder[AuxiliaryData] = this
  def providerStrategy(x: ProviderStrategy[AuxiliaryData]): ServiceProviderBuilder[AuxiliaryData] = this
  def serviceName(x: String): ServiceProviderBuilder[AuxiliaryData] = this
  def getInstance(): ServiceInstance[AuxiliaryData] =
    ServiceInstance.builder[AuxiliaryData].
      name("name").
      port(80).
      build()

  def close(): Unit = fail("No close")
  def queryForInstance(x: String, y: String): ServiceInstance[AuxiliaryData] = fail("No queryForInstance")
  def queryForInstances(x: String): Collection[ServiceInstance[AuxiliaryData]] = fail("No queryForInstances")
  def queryForNames(): Collection[String] = fail("No queryForNames")
  def registerService(x: ServiceInstance[AuxiliaryData]): Unit = fail("No registerService")
  def serviceCacheBuilder(): ServiceCacheBuilder[AuxiliaryData] = fail("No serviceCacheBuilder")
  def start(): Unit = fail("No start")
  def unregisterService(x: ServiceInstance[AuxiliaryData]): Unit = fail("No unregisterService")
  def updateService(x: ServiceInstance[AuxiliaryData]): Unit = fail("No updateService")

  def additionalFilter(x: InstanceFilter[AuxiliaryData]): ServiceProviderBuilder[AuxiliaryData] = fail("No additionalFilter")
  def build(): ServiceProvider[AuxiliaryData] = this
  def downInstancePolicy(x: DownInstancePolicy): ServiceProviderBuilder[AuxiliaryData] = fail("No downInstancePolicy")
  def threadFactory(x: ThreadFactory): ServiceProviderBuilder[AuxiliaryData] = fail("No threadFactory")

  def getAllInstances(): Collection[ServiceInstance[AuxiliaryData]] = fail("No getAllInstances")
  def noteError(x: ServiceInstance[AuxiliaryData]): Unit = fail("No noteError")
}
