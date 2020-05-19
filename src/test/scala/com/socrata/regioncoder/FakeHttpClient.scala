package com.socrata.regioncoder

import scala.collection.mutable
import org.scalatest.FunSuiteLike

import com.socrata.http.client.{HttpClient, SimpleHttpRequest, ResponseInfo, BodylessHttpRequest, JsonHttpRequest}
import java.io.{ByteArrayInputStream, Closeable}
import java.nio.charset.StandardCharsets
import java.net.URLEncoder
import javax.servlet.http.HttpServletResponse

import com.rojoma.json.v3.io.JsonReader

object FakeHttpClient {
  case class Result(status: Int = HttpServletResponse.SC_OK,
                    contentType: String,
                    content: String)

  type Handler = (Map[String, String], Option[String]) => Option[Result]

  class Builder private[FakeHttpClient] (responses: Map[String, Map[String, Vector[Handler]]]) {
    def register(method: String, path: String, result: Handler): Builder = {
      responses.get(path) match {
        case None =>
          new Builder(responses + (path -> Map(method -> Vector(result))))
        case Some(v) =>
          v.get(method) match {
            case None =>
              new Builder(responses + (path -> (v + (method -> Vector(result)))))
            case Some(handlers) =>
              new Builder(responses + (path -> (v + (method -> (handlers :+ result)))))
          }
      }
    }

    def build = new FakeHttpClient(responses)
  }

  def builder() = new Builder(Map.empty)
}

class FakeHttpClient private (responses: Map[String, Map[String, Seq[FakeHttpClient.Handler]]]) extends HttpClient with Failable {
  import FakeHttpClient._

  override def close(): Unit = fail("No close")

  private def blankResponse(status: Int) =
    new RawResponse with Closeable {
      def close() {}
      val responseInfo = new ResponseInfo {
        val resultCode = status
        def headers(name: String) = Array[String]()
        val headerNames = Set.empty[String]
      }
      val body = new ByteArrayInputStream(Array[Byte]())
    }

  private def runHandlers(handlers: Seq[Handler], query: Map[String, String], body: Option[String]): Result = {
    for {
      handler <- handlers
      result <- handler(query, body)
    } return result

    Result(status = HttpServletResponse.SC_BAD_REQUEST,
           contentType = "text/plain",
           content = "No handler found")
  }

  override def executeRawUnmanaged(req: SimpleHttpRequest): RawResponse with Closeable = {
    val method = req.builder.method
    val path = req.builder.path.map(URLEncoder.encode(_, "UTF-8")).mkString("/")
    val query = req.builder.query.toMap

    responses.get(path) match {
      case Some(methods) =>
        methods.get(method.get) match {
          case Some(handlers) =>
            new RawResponse with Closeable {
              val result =
                req match {
                  case _ : BodylessHttpRequest =>
                    runHandlers(handlers, query, None)
                  case json: JsonHttpRequest =>
                    runHandlers(handlers, query, Some(JsonReader.fromEvents(json.contents).toString))
                  case _ =>
                    fail("Bad request")
                }
              def close() {}
              val responseInfo = new ResponseInfo {
                val resultCode = result.status
                def headers(name: String) = name match {
                  case "Content-type" => Array(result.contentType)
                  case _ => Array[String]()
                }
                val headerNames = Set("Content-type")
              }
              val body = new ByteArrayInputStream(result.content.getBytes(StandardCharsets.UTF_8))
            }
          case None =>
            blankResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
        }
      case None =>
        blankResponse(HttpServletResponse.SC_NOT_FOUND)
    }
  }
}
