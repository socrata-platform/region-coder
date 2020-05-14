package com.socrata.regioncoder

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import com.socrata.regioncoder.config.RegionCoderConfig
import com.typesafe.config.ConfigFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.socrata.http.server.HttpRequest
import com.rojoma.simplearm.v2._
import com.rojoma.json.v3.interpolation._
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import com.socrata.http.common.AuxiliaryData
import java.io.Closeable
import com.socrata.geospace.lib.client.SodaResponse

object RegionCoderServletSpec {
  val config = """
                 | geospace.cache.enable-depressurize = false
                 | geospace.partitioning.sizeX = 5.0
                 | geospace.partitioning.sizeY = 5.0
               """.stripMargin
}

// scalastyle:off multiple.string.literals
class RegionCoderServletSpec extends FunSuiteLike with RegionCoderMockResponses with Matchers {
  val cfg = new RegionCoderConfig(ConfigFactory.parseString(RegionCoderServletSpec.config).
    withFallback(ConfigFactory.load().getConfig("com.socrata")))

  test("index page") {
    withResp("/") { resp =>
      resp.status should equal (HttpServletResponse.SC_OK)
    }
  }

  test("version") {
    withResp("/version") { resp =>
      resp.status should equal (HttpServletResponse.SC_OK)
    }
  }

  // Pretty much an end to end functional test, from Servlet route to SF client and region cache
  test("v2 - points region code properly with cache loaded from soda fountain mock") {
    withResp("/v2/regions/triangles/pointcode?columnToReturn=user_defined_key",
             content = "[[0.1, 0.5], [0.5, 0.1], [4.99, 4.99]]",
             mocks = Seq(mockSodaSchema("triangles"),
                         mockSodaIntersects("triangles.geojson", "0", "0", geojson),
                         mockSodaEmptyIntersects("triangles.geojson")))
    { resp =>
      resp.status should equal (HttpServletResponse.SC_OK)
      resp.json should equal (json"""[101,102,null]""")
    }
  }

  test("v2 - points in multiple partitions region code properly with cache loaded from soda fountain") {
    withResp("/v2/regions/triangles/pointcode?columnToReturn=user_defined_key",
             content =  "[[0.1, 0.5], [11.1, 13.9], [0.5, 0.1]]",
             mocks = Seq(mockSodaSchema("triangles"),
                         mockSodaIntersects("triangles.geojson", "0", "0", geojson),
                         mockSodaIntersects("triangles.geojson", "8", "12", geojson2))) { resp =>
      resp.status should equal (HttpServletResponse.SC_OK)
      resp.json should equal (json"""[101,104,102]""")
    }
  }

  test("v2 - string coding service") {
    mockSodaRoute("triangles.geojson", geojson)

    withResp("/v2/regions/triangles/stringcode?columnToMatch=name&columnToReturn=user_defined_key",
             content = """["My MiXeD CaSe NaMe 1", "another NAME", "My MiXeD CaSe NaMe 2"]""",
             mocks = Seq(mockSodaRoute("triangles.geojson", geojson))) { resp =>
      resp.status should equal (HttpServletResponse.SC_OK)
      resp.json should equal (json"""[101,null,102]""")
    }
  }

  test("region coding service should return 500 if soda fountain server down") {
    try {
      withResp("/v2/regions/triangles/pointcode?columnToReturn=user_defined_key",
               content = "[[0.1, 0.5], [0.5, 0.1], [10, 20]]") { resp =>
        fail("Should have thrown")
      }
    } catch {
      case _ : SodaResponse.UnexpectedResponseCode =>
        // ok, this is what we're looking for
    }
  }

  test("region coding service should return 500 if soda fountain server returns something unexpected (non-JSON)") {
    try {
      withResp("/v2/regions/nonsense/pointcode?columnToReturn=user_defined_key",
               content = "[[0.1, 0.5], [0.5, 0.1], [10, 20]]",
               mocks = Seq(mockSodaSchema("nonsense"),
                           mockSodaRoute("nonsense.geojson", "gobbledygook"))) { resp =>
        fail("Should have thrown")
      }
    } catch {
      case _ : SodaResponse.JsonParseException =>
        // ok, this is what we're looking for
    }
  }
}
