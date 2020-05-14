package com.socrata.regioncoder

import java.util.Collection
import javax.servlet.http._
import javax.servlet.{ServletOutputStream, WriteListener}
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import com.rojoma.json.v3.io.JsonReader
import com.rojoma.json.v3.ast.JValue

class RecordingHttpServletResponse extends HttpServletResponse with Failable {
  def addCookie(x: Cookie): Unit = fail("No addCookie")
  def addDateHeader(x: String, y: Long): Unit = fail("No addDateHeader")
  def addHeader(x: String,y: String): Unit = fail("No addHeader")
  def addIntHeader(x: String,y: Int): Unit = fail("No addIntHeader")
  def containsHeader(x: String): Boolean = fail("No containsHeader")
  def encodeRedirectURL(x: String): String = fail("No encodeRedirectURL")
  def encodeRedirectUrl(x: String): String = fail("No encodeRedirectUrl")
  def encodeURL(x: String): String = fail("No encodeURL")
  def encodeUrl(x: String): String = fail("No encodeUrl")
  def getHeader(x: String): String = fail("No getHeader")
  def getHeaderNames(): Collection[String] = fail("No getHeaderNames")
  def getHeaders(x: String): Collection[String] = fail("No getHeaders")
  def getStatus(): Int = status
  def sendError(x: Int): Unit = fail("No sendError")
  def sendError(x: Int,y: String): Unit = fail("No sendError")
  def sendRedirect(x: String): Unit = fail("No sendRedirect")
  def setDateHeader(x: String,y: Long): Unit = fail("No setDateHeader")
  def setHeader(x: String,y: String): Unit = fail("No setHeader")
  def setIntHeader(x: String,y: Int): Unit = fail("No setIntHeader")
  def setStatus(x: Int,y: String): Unit = fail("No setStatus")
  def setStatus(x: Int): Unit = status = x

  // Members declared in javax.servlet.ServletResponse
  def flushBuffer(): Unit = fail("No flushBuffer")
  def getBufferSize(): Int = fail("No getBufferSize")
  def getCharacterEncoding(): String = fail("No getCharacterEncoding")
  def getContentType(): String = fail("No getContentType")
  def getLocale(): java.util.Locale = fail("No getLocale")
  def getOutputStream(): ServletOutputStream = theOutputStream
  def getWriter(): java.io.PrintWriter = fail("No getWriter")
  def isCommitted(): Boolean = fail("No isCommitted")
  def reset(): Unit = fail("No reset")
  def resetBuffer(): Unit = fail("No resetBuffer")
  def setBufferSize(x: Int): Unit = fail("No setBufferSize")
  def setCharacterEncoding(x: String): Unit = fail("No setCharacterEncoding")
  def setContentLength(x: Int): Unit = fail("No setContentLength")
  def setContentLengthLong(x: Long): Unit = fail("No setContentLengthLong")
  def setContentType(x: String): Unit = contentType = x
  def setLocale(x: java.util.Locale): Unit = fail("No setLocale")

  private val theBytes = new ByteArrayOutputStream
  private lazy val theOutputStream =
    new ServletOutputStream {
      override def write(x: Int) = theBytes.write(x)
      override def isReady = true
      override def setWriteListener(x: WriteListener) = fail("No setWriteListener")
    }

  var status = HttpServletResponse.SC_OK
  var contentType = "application/octet-stream"
  def json: JValue = JsonReader.fromString(new String(theBytes.toByteArray, StandardCharsets.UTF_8))
}
