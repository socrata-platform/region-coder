package com.socrata.regioncoder

import com.socrata.regioncoder.config.RegionCoderConfig
import javax.servlet.http.{HttpServletResponse => HttpStatus}
import org.scalatra.AsyncResult
import com.socrata.soda.external.SodaFountainClient

class RegionCoderServlet(rcConfig: RegionCoderConfig, val sodaFountain: SodaFountainClient)
  extends RegionCoderStack with RegionCoder {

  val cacheConfig = rcConfig.cache
  val partitionXsize = rcConfig.partitioning.sizeX
  val partitionYsize = rcConfig.partitioning.sizeY

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  get("/version") {
    Map("version" -> BuildInfo.version,
      "scalaVersion" -> BuildInfo.scalaVersion,
      "dependencies" -> BuildInfo.libraryDependencies,
      "buildTime" -> new org.joda.time.DateTime(BuildInfo.buildTime).toString())
  }

  // Request body is a JSON array of points. Each point is an array of length 2.
  // Example: [[-87.6847,41.8369],[-122.3331,47.6097]]
  post("/v1/regions/:resourceName/regioncode") {
    val points = parsedBody.extract[Seq[Seq[Double]]]
    if (points.isEmpty) {
      halt(HttpStatus.SC_BAD_REQUEST, s"Could not parse '${request.body}'.  Must be in the form [[x, y]...]")
    }
    new AsyncResult {
      override val timeout = rcConfig.shapePayloadTimeout
      val is = timer.time { geoRegionCode(params("resourceName"), points) }
    }
  }
}
