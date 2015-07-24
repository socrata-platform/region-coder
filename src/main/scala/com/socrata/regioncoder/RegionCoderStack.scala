package com.socrata.regioncoder

import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.slf4j.{MDC, LoggerFactory}

trait RegionCoderStack extends ScalatraServlet
  with JacksonJsonSupport with FutureSupport with ScalatraLogging {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit def jsonFormats: Formats =
    DefaultFormats + new NoneSerializer + new EnvelopeSerializer

  // For FutureSupport / async stuff
  protected implicit val executor = concurrent.ExecutionContext.Implicits.global

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  error {
    case e: Exception =>
      logger.error("Request errored out", e)
      InternalServerError(s"${e.getClass.getSimpleName}: ${e.getMessage}\n${e.getStackTraceString}\n")
  }
}

// TODO: Move this to thirdparty-utils
trait ScalatraLogging extends ScalatraServlet {
  val logger = LoggerFactory.getLogger(getClass)
  before() {
    // TODO : Generate a request ID if one wasn't passed in
    request.header("X-Socrata-RequestId").foreach( // scalastyle:off multiple.string.literals
      MDC.put("X-Socrata-RequestId", _))

    logger.info(request.getMethod + " - " + request.getRequestURI + " ? " + request.getQueryString)
  }

  after() {
    logger.info("Status - " + response.getStatus)
  }
}
