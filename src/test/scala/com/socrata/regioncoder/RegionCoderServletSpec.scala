package com.socrata.regioncoder

import com.github.tomakehurst.wiremock.client.WireMock
import com.socrata.regioncoder.config.RegionCoderConfig
import com.typesafe.config.ConfigFactory
import javax.servlet.http.{HttpServletResponse => HttpStatus}
import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest.ScalatraSuite

object RegionCoderServletSpec {
  val config = """
                 | geospace.cache.enable-depressurize = false
                 | geospace.partitioning.sizeX = 5.0
                 | geospace.partitioning.sizeY = 5.0
               """.stripMargin
}

// scalastyle:off multiple.string.literals
class RegionCoderServletSpec extends ScalatraSuite with FunSuiteLike with RegionCoderMockResponses {
  mockServerPort = 51234 // scalastyle:ignore magic.number

  override def beforeAll(): Unit = {
    super.beforeAll()
    val cfg = ConfigFactory.parseString(RegionCoderServletSpec.config).
      withFallback(ConfigFactory.load().getConfig("com.socrata"))
    addServlet(new RegionCoderServlet(new RegionCoderConfig(cfg), sodaFountain), "/*")
  }

  test("index page") {
    get("/") {
      status should equal (HttpStatus.SC_OK)
    }
  }

  test("version") {
    get("/version") {
      status should equal (HttpStatus.SC_OK)
    }
  }

  // Pretty much an end to end functional test, from Servlet route to SF client and region cache
  test("v1 - points region code properly with cache loaded from soda fountain mock") {
    forceRegionRecache()
    mockSodaSchema("triangles")
    mockSodaIntersects("triangles.geojson", "0", "0", geojson)

    post("/v1/regions/triangles/pointcode",
      "[[0.1, 0.5], [0.5, 0.1], [4.99, 4.99]]",
      headers = Map("Content-Type" -> "application/json")) {
      status should equal (HttpStatus.SC_OK)
      body should equal ("""[1,2,null]""")
    }
  }

  test("v1 - points in multiple partitions region code properly with cache loaded from soda fountain") {
    forceRegionRecache()
    mockSodaSchema("triangles")
    mockSodaIntersects("triangles.geojson", "0", "0", geojson)
    mockSodaIntersects("triangles.geojson", "8", "12", geojson2)

    post("/v1/regions/triangles/pointcode",
      "[[0.1, 0.5], [11.1, 13.9], [0.5, 0.1]]",
      headers = Map("Content-Type" -> "application/json")) {
      status should equal (HttpStatus.SC_OK)
      body should equal ("""[1,4,2]""")
    }
  }

  test("v2 - points region code properly with cache loaded from soda fountain mock") {
    forceRegionRecache()
    mockSodaSchema("triangles")
    mockSodaIntersects("triangles.geojson", "0", "0", geojson)

    post("/v2/regions/triangles/pointcode?columnToReturn=user_defined_key",
      "[[0.1, 0.5], [0.5, 0.1], [4.99, 4.99]]",
      headers = Map("Content-Type" -> "application/json")) {
      status should equal (HttpStatus.SC_OK)
      body should equal ("""[101,102,null]""")
    }
  }

  test("v1 - string coding service") {
    forceRegionRecache()
    mockSodaRoute("triangles.geojson", geojson)

    post("/v1/regions/triangles/stringcode?column=name",
      """["My MiXeD CaSe NaMe 1", "another NAME", "My MiXeD CaSe NaMe 2"]""",
      headers = Map("Content-Type" -> "application/json")) {
      status should equal (HttpStatus.SC_OK)
      body should equal ("""[1,null,2]""")
    }
  }

  test("v2 - string coding service") {
    forceRegionRecache()
    mockSodaRoute("triangles.geojson", geojson)

    post("/v2/regions/triangles/stringcode?columnToMatch=name&columnToReturn=user_defined_key",
      """["My MiXeD CaSe NaMe 1", "another NAME", "My MiXeD CaSe NaMe 2"]""",
      headers = Map("Content-Type" -> "application/json")) {
      status should equal (HttpStatus.SC_OK)
      body should equal ("""[101,null,102]""")
    }
  }

  test("region coding service should return 500 if soda fountain server down") {
    forceRegionRecache()
    WireMock.reset()

    post("/v1/regions/triangles/pointcode",
      "[[0.1, 0.5], [0.5, 0.1], [10, 20]]",
      headers = Map("Content-Type" -> "application/json")) {
      status should equal (HttpStatus.SC_INTERNAL_SERVER_ERROR)
    }
  }

  test("region coding service should return 500 if soda fountain server returns something unexpected (non-JSON)") {
    forceRegionRecache()
    mockSodaRoute("nonsense.geojson", "gobbledygook")

    post("/v1/regions/nonsense/pointcode",
      "[[0.1, 0.5], [0.5, 0.1], [10, 20]]",
      headers = Map("Content-Type" -> "application/json")) {
      status should equal (HttpStatus.SC_INTERNAL_SERVER_ERROR)
    }
  }
}
