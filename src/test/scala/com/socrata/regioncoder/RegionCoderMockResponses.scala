package com.socrata.regioncoder

import com.github.tomakehurst.wiremock.client.WireMock
import javax.servlet.http.{HttpServletResponse => HttpStatus}

// scalastyle:off multiple.string.literals
trait RegionCoderMockResponses extends FakeSodaFountain {
  protected def mockSodaRoute(resourceName: String, returnedBody: String): Unit = {
    WireMock.stubFor(WireMock.get(WireMock.urlMatching(s"/resource/$resourceName??.*")).
      willReturn(WireMock.aResponse()
      .withStatus(HttpStatus.SC_OK)
      .withHeader("Content-Type", "application/vnd.geo+json; charset=utf-8")
      .withBody(returnedBody)))
  }

  protected def mockSodaIntersects(
    resourceName: String, x: String, y: String, returnedBody: String): Unit = {
    WireMock.stubFor(WireMock.get(WireMock.urlMatching(s"/resource/$resourceName??.*POLYGON%20.*$x...$y.*")).
      willReturn(WireMock.aResponse()
      .withStatus(HttpStatus.SC_OK)
      .withHeader("Content-Type", "application/vnd.geo+json; charset=utf-8")
      .withBody(returnedBody)))
  }

  protected def mockSodaSchema(
    resourceName: String, columnName: String = "the_geom"): Unit = {
    val body = s"""{"columns":{"$columnName":{"datatype":"multipolygon"}}}"""
    WireMock.stubFor(WireMock.get(WireMock.urlMatching(s"/dataset/$resourceName")).
      willReturn(WireMock.aResponse()
      .withStatus(HttpStatus.SC_OK)
      .withHeader("Content-Type", "application/json; charset=utf-8")
      .withBody(body)))
  }

  protected def forceRegionRecache(): Unit = {
    // Reset the cache to force region to load from soda fountain
    delete("/v2/regions") {
      status should equal (HttpStatus.SC_OK)
    }

    // Verify the cache is empty
    get("/v2/regions") {
      body should equal ("""{"spatialCache":[],"stringCache":[]}""")
    }
  }

  val feat1 = """{
                |  "type": "Feature",
                |  "geometry": {
                |    "type": "Polygon",
                |    "coordinates": [[[0.0, 0.0], [0.0, 1.0], [1.0, 1.0], [0.0, 0.0]]]
                |  },
                |  "properties": { "_feature_id": "1", "user_defined_key": "101", "name": "My Mixed Case Name 1" }
                |}""".stripMargin
  val feat2 = """{
                |  "type": "Feature",
                |  "geometry": {
                |    "type": "Polygon",
                |    "coordinates": [[[0.0, 0.0], [1.0, 0.0], [1.0, 1.0], [0.0, 0.0]]]
                |  },
                |  "properties": { "_feature_id": "2", "user_defined_key": "102", "name": "My Mixed Case Name 2" }
                |}""".stripMargin
  val feat3 = """{
                |  "type": "Feature",
                |  "geometry": {
                |    "type": "Polygon",
                |    "coordinates": [[[10.0, 10.0], [10.0, 15.0], [15.0, 15.0], [10.0, 10.0]]]
                |  },
                |  "properties": { "_feature_id": "4", "user_defined_key": "104", "name": "My Mixed Case Name 2" }
                |}""".stripMargin
  val geojson = """{"type":"FeatureCollection",
                  |"crs" : { "type": "name", "properties": { "name": "urn:ogc:def:crs:OGC:1.3:CRS84" } },
                  |"features": [""".stripMargin + Seq(feat1, feat2).mkString(",") + "]}"
  val geojson2 = """{"type":"FeatureCollection",
                   |"crs" : { "type": "name", "properties": { "name": "urn:ogc:def:crs:OGC:1.3:CRS84" } },
                   |"features": [""".stripMargin + Seq(feat3).mkString(",") + "]}"
}
