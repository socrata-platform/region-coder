package com.socrata.regioncoder

import org.apache.curator.x.discovery._
import com.socrata.http.common.AuxiliaryData

trait FakeDiscovery extends Failable {
  val fakeDiscovery = new ServiceDiscovery[AuxiliaryData] with ServiceProviderBuilder[AuxiliaryData] with ServiceProvider[AuxiliaryData] {

    def close(): Unit = fail("No close")
    def queryForInstance(x$1: String,x$2: String): ServiceInstance[AuxiliaryData] = fail("No queryForInstance")
    def queryForInstances(x$1: String): java.util.Collection[ServiceInstance[AuxiliaryData]] = fail("No queryForInstances")
    def queryForNames(): java.util.Collection[String] = fail("No queryForNames")
    def registerService(x$1: ServiceInstance[AuxiliaryData]): Unit = fail("No registerService")
    def serviceCacheBuilder(): ServiceCacheBuilder[AuxiliaryData] = fail("No serviceCacheBuilder")
    def serviceProviderBuilder(): ServiceProviderBuilder[AuxiliaryData] = this
    def start(): Unit = fail("No start")
    def unregisterService(x$1: ServiceInstance[AuxiliaryData]): Unit = fail("No unregisterService")
    def updateService(x$1: ServiceInstance[AuxiliaryData]): Unit = fail("No updateService")

    def additionalFilter(x$1: InstanceFilter[AuxiliaryData]): ServiceProviderBuilder[com.socrata.http.common.AuxiliaryData] = fail("No additionalFilter")
    def build(): ServiceProvider[AuxiliaryData] = this
    def downInstancePolicy(x$1: DownInstancePolicy): ServiceProviderBuilder[AuxiliaryData] = fail("No downInstancePolicy")
    def providerStrategy(x$1: ProviderStrategy[AuxiliaryData]): ServiceProviderBuilder[AuxiliaryData] = this
    def serviceName(x$1: String): ServiceProviderBuilder[AuxiliaryData] = this
    def threadFactory(x$1: java.util.concurrent.ThreadFactory): ServiceProviderBuilder[com.socrata.http.common.AuxiliaryData] = fail("No threadFactory")

    def getAllInstances(): java.util.Collection[ServiceInstance[AuxiliaryData]] = fail("No getAllInstances")
    def getInstance(): ServiceInstance[AuxiliaryData] =
      ServiceInstance.builder[AuxiliaryData].
        name("name").
        port(80).
        build()
    def noteError(x$1: ServiceInstance[AuxiliaryData]): Unit = fail("No noteError")
  }
}
