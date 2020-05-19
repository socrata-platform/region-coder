package com.socrata.regioncoder

import java.util.concurrent.ForkJoinPool
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

import com.codahale.metrics.MetricRegistry
import nl.grons.metrics.scala.InstrumentedBuilder
import com.rojoma.json.v3.ast.{JValue, JObject, JString, JNull}
import com.rojoma.json.v3.codec.JsonEncode
import com.rojoma.json.v3.util.JsonUtil
import com.socrata.geospace.lib.Utils._
import com.socrata.regioncoder.config.RegionCoderConfig
import com.socrata.soda.external.SodaFountainClient
import com.socrata.http.server.{HttpRequest, HttpResponse}
import com.socrata.http.server.routing._
import com.socrata.http.server.responses._
import com.socrata.http.server.implicits._
import com.rojoma.json.v3.interpolation._
import org.slf4j.LoggerFactory

// scalastyle:off multiple.string.literals
class RegionCoderServlet(rcConfig: RegionCoderConfig, val sodaFountain: SodaFountainClient, val metricRegistry: MetricRegistry)
  extends RegionCoder with InstrumentedBuilder {
  val logger = LoggerFactory.getLogger(classOf[RegionCoderServlet])

  val concurrencyPerJob = rcConfig.threadPoolLimit
  val cacheConfig = rcConfig.cache
  val partitionXsize = rcConfig.partitioning.sizeX
  val partitionYsize = rcConfig.partitioning.sizeY

  val pointcodeTimer = metrics.timer("pointcode")
  val stringcodeTimer = metrics.timer("stringcode")
  val debugGetTimer = metrics.timer("debug-get")
  val debugClearTimer = metrics.timer("debug-clear")

  def badRequest(msg: String) =
    BadRequest ~> Content("text/plain", msg)

  def ok[T : JsonEncode](value: T) =
    OK ~> Json(value)

  def root =
    new SimpleResource {
      override val get = { (req: HttpRequest) => ok(json"""{ hello: "region-coder" }""") }
    }

  def version =
    new SimpleResource {
      override val get = { (req: HttpRequest) => ok(BuildInfo.toMap.mapValues(v => JString(v.toString))) }
    }

  def encodeOrNull[T : JsonEncode](x: Option[T]): JValue =
    x match {
      case Some(t) => JsonEncode.toJValue(t)
      case None => JNull
    }

  def pointcodeV2(resourceName: String) =
    new SimpleResource {
      override val post = doPointcode _

      private def doPointcode(req: HttpRequest): HttpResponse = {
        val points =
          JsonUtil.readJson[Vector[(Double, Double)]](new InputStreamReader(req.inputStream, StandardCharsets.UTF_8)).right.getOrElse {
            return badRequest("Could not parse request.  Must be in the form [[x, y],[a,b],...]")
          }
        val columnToReturn = req.queryParameter("columnToReturn").getOrElse {
          return badRequest("Missing param 'columnToReturn'")
        }
        pointcodeTimer.time {
          ok(regionCodeByPoint(
               resourceName,
               columnToReturn,
               points,
               (featureId: String) => featureId.toInt
             ).map(encodeOrNull[Int]))
        }
      }
    }

  // This is a newer version of the the v2 pointcode call
  // it behaves in the same way, but doesn't try to turn the cache's return value
  // into an int, it just passes it back as a string.
  def pointcodeV3(resourceName: String) =
    new SimpleResource {
      override val post = doPointcode _

      private def doPointcode(req: HttpRequest): HttpResponse = {
        val points =
          JsonUtil.readJson[Vector[(Double, Double)]](new InputStreamReader(req.inputStream, StandardCharsets.UTF_8)).right.getOrElse {
            return badRequest("Could not parse request.  Must be in the form [[x, y],[a,b],...]")
          }
        val columnToReturn = req.queryParameter("columnToReturn").getOrElse {
          return badRequest("Missing param 'columnToReturn'")
        }
        pointcodeTimer.time {
          ok(regionCodeByPoint(
               resourceName,
               columnToReturn,
               points,
               (featureId: String) => featureId
             ).map(encodeOrNull[String]))
        }
      }
    }

  def stringcode(resourceName: String) =
    new SimpleResource {
      override val post = doStringcode _

      private def doStringcode(req: HttpRequest): HttpResponse = {
        val strings = JsonUtil.readJson[Seq[String]](new InputStreamReader(req.inputStream, StandardCharsets.UTF_8)).right.getOrElse {
          return badRequest("""Could not parse request.  Must be in the form ["98102","98101",...]""")
        }
        val columnToMatch = req.queryParameter("columnToMatch").getOrElse {
          return badRequest("Missing param 'columnToMatch'")
        }
        val columnToReturn = req.queryParameter("columnToReturn").getOrElse {
          return badRequest("Missing param 'columnToReturn'")
        }
        stringcodeTimer.time {
          ok(regionCodeByString(resourceName, columnToMatch, columnToReturn, strings).map(encodeOrNull[Int]))
        }
      }
    }

  def regions(version: String) =
    new SimpleResource {
      // DEBUGGING ROUTE : Returns a JSON blob with info about all currently cached regions, ordered by least-recently-used
      override val get = { (req: HttpRequest) =>
        debugGetTimer.time {
          ok(Map(
            "spatialCache" -> spatialCache.entriesByLeastRecentlyUsed().map {
              case (key, size) => json"""{ resource: $key, numCoordinates: $size }"""
            },
            "stringCache" -> stringCache.entriesByLeastRecentlyUsed().map {
              case (key, size) => json"""{ resource: $key, numRows: $size }"""
            }))
        }
      }

      override val delete = { (req: HttpRequest) =>
        debugClearTimer.time {
          resetRegionState()
          logMemoryUsage("After clearing region caches")
          OK ~> Content("text/plain", "Done")
        }
      }
    }


  val routeContext = new RouteContext[HttpRequest, HttpResponse]
  import routeContext._

  val routes =
    Routes(
      Route("/", root),
      Route("/version", version),
      Route("/v2/regions/?/pointcode", pointcodeV2 _),
      Route("/v3/regions/?/pointcode", pointcodeV3 _),
      Route("/v2/regions/?/stringcode", stringcode _),
      Route("/?/regions", regions _))

  def handle(req: HttpRequest): HttpResponse = {
    routes(req.requestPath) match {
      case Some(service) => service(req)
      case None => NotFound
    }
  }
}
