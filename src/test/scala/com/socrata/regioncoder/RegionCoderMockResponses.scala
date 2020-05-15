package com.socrata.regioncoder

import javax.servlet.http.{HttpServletResponse => HttpStatus}
import com.rojoma.simplearm.v2._
import com.rojoma.json.v3.interpolation._
import com.socrata.http.server.HttpRequest
import com.socrata.regioncoder.config.RegionCoderConfig
import org.scalatest.Matchers

// scalastyle:off multiple.string.literals
trait RegionCoderMockResponses extends Matchers {
  val cfg: RegionCoderConfig

  case class Mock(method: String, path: String, response: FakeHttpClient.Handler)

  def withResp[T](url: String, content: String = null, method: String = null, mocks: Seq[Mock] = Nil)(f: RecordingHttpServletResponse => T): T = {
    val rawReq = new FakeHttpServletRequest(url, Option(content), Option(method))
    using(new ResourceScope) { rs =>
      val req = new HttpRequest {
        override val servletRequest = new HttpRequest.AugmentedHttpServletRequest(rawReq)
        override val resourceScope = rs
      }
      val resp = new RecordingHttpServletResponse

      val http = new FakeHttpClient

      for(mock <- mocks) {
        http.register(mock.method, mock.path, mock.response)
      }

      val regionCoderServlet = new RegionCoderServlet(cfg, new FakeSodaFountain(http.fakeHttpClient, cfg).fakeSodaFountain)
      regionCoderServlet.handle(req)(resp)
      f(resp)
    }
  }

  protected def mockSodaRoute(resourceName: String, returnedBody: String): Mock = {
    Mock(
      method = "GET",
      path = s"resource/$resourceName",
      response = { (_, _) =>
        Some(FakeHttpClient.Result(
               status = HttpStatus.SC_OK,
               contentType = "application/vnd.geo+json; charset=utf-8",
               content = returnedBody
             ))
      }
    )
  }

  protected def mockSodaIntersects(resourceName: String, x: String, y: String, returnedBody: String): Mock = {
    Mock(
      method = "GET",
      path = s"resource/$resourceName",
      response = { (query, _) =>
        if(query.get("$query").fold(false) { q => q.contains(s"$x $y") }) {
          Some(FakeHttpClient.Result(
                 status = HttpStatus.SC_OK,
                 contentType = "application/vnd.geo+json; charset=utf-8",
                 content = returnedBody
               ))
        } else {
          None
        }
      }
    )
  }

  protected def mockSodaEmptyIntersects(resourceName: String): Mock = {
    Mock(
      method = "GET",
      path = s"resource/$resourceName",
      response = { (_, _) =>
        Some(FakeHttpClient.Result(
               status = HttpStatus.SC_OK,
               contentType = "application/vnd.geo+json; charset=utf-8",
               content = emptyGeojson
             ))
      }
    )
  }

  protected def mockSodaSchema(resourceName: String, columnName: String = "the_geom"): Mock = {
    Mock(
      method = "GET",
      path = s"dataset/$resourceName",
      response = { (_, _) =>
        Some(FakeHttpClient.Result(
          status = HttpStatus.SC_OK,
          contentType = "application/json; charset=utf-8",
          content = json"""{"columns":{$columnName:{"datatype":"multipolygon"}}}""".toString
        ))
      }
    )
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
  val emptyGeojson = """{"type":"FeatureCollection",
                  |"crs" : { "type": "name", "properties": { "name": "urn:ogc:def:crs:OGC:1.3:CRS84" } },
                  |"features": []}""".stripMargin
  val geojson2 = """{"type":"FeatureCollection",
                   |"crs" : { "type": "name", "properties": { "name": "urn:ogc:def:crs:OGC:1.3:CRS84" } },
                   |"features": [""".stripMargin + Seq(feat3).mkString(",") + "]}"
}
