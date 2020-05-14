package com.socrata.regioncoder

import org.scalatest.FunSuiteLike

import com.socrata.http.client.HttpClient
import com.socrata.soda.external.SodaFountainClient
import com.socrata.curator.ServerProvider.RetryOnAllExceptionsDuringInitialRequest
import com.socrata.regioncoder.config.RegionCoderConfig

class FakeSodaFountain(httpClient: HttpClient, cfg: RegionCoderConfig) extends FakeDiscovery with Failable {
  val fakeSodaFountain = new SodaFountainClient(
    httpClient,
    fakeDiscovery,
    cfg.sodaFountain.serviceName,
    cfg.curator.connectTimeout,
    cfg.sodaFountain.maxRetries,
    RetryOnAllExceptionsDuringInitialRequest,
    fail("Service discovery failed")
  )
}

