package com.socrata.regioncoder

import java.util.{Collection, Locale}
import javax.servlet.http._
import javax.servlet.{ServletOutputStream, WriteListener}
import java.io.{ByteArrayOutputStream, PrintWriter}
import java.nio.charset.StandardCharsets
import com.rojoma.json.v3.io.JsonReader
import com.rojoma.json.v3.ast.JValue

class RecordingHttpServletResponse extends HttpServletResponse with Failable {
  private val theBytes = new ByteArrayOutputStream
  private val theOutputStream =
    new ServletOutputStream {
      override def write(x: Int) = theBytes.write(x)
      override def isReady = true
      override def setWriteListener(x: WriteListener) = fail("No setWriteListener")
    }

  var status = HttpServletResponse.SC_OK
  var contentType = "application/octet-stream"
  def json: JValue = JsonReader.fromString(new String(theBytes.toByteArray, StandardCharsets.UTF_8))

  override def getStatus(): Int = status
  override def setStatus(x: Int): Unit = status = x

  override def getOutputStream(): ServletOutputStream = theOutputStream

  override def setContentType(x: String): Unit = contentType = x

  // Members declared in HttpServletResponse
  override def addCookie(x: Cookie): Unit = fail("No addCookie")
  override def addDateHeader(x: String, y: Long): Unit = fail("No addDateHeader")
  override def addHeader(x: String,y: String): Unit = fail("No addHeader")
  override def addIntHeader(x: String,y: Int): Unit = fail("No addIntHeader")
  override def containsHeader(x: String): Boolean = fail("No containsHeader")
  override def encodeRedirectURL(x: String): String = fail("No encodeRedirectURL")
  override def encodeRedirectUrl(x: String): String = fail("No encodeRedirectUrl")
  override def encodeURL(x: String): String = fail("No encodeURL")
  override def encodeUrl(x: String): String = fail("No encodeUrl")
  override def getHeader(x: String): String = fail("No getHeader")
  override def getHeaderNames(): Collection[String] = fail("No getHeaderNames")
  override def getHeaders(x: String): Collection[String] = fail("No getHeaders")
  override def sendError(x: Int): Unit = fail("No sendError")
  override def sendError(x: Int,y: String): Unit = fail("No sendError")
  override def sendRedirect(x: String): Unit = fail("No sendRedirect")
  override def setDateHeader(x: String,y: Long): Unit = fail("No setDateHeader")
  override def setHeader(x: String,y: String): Unit = fail("No setHeader")
  override def setIntHeader(x: String,y: Int): Unit = fail("No setIntHeader")
  override def setStatus(x: Int,y: String): Unit = fail("No setStatus")

  // Members declared in javax.servlet.ServletResponse
  override def flushBuffer(): Unit = fail("No flushBuffer")
  override def getBufferSize(): Int = fail("No getBufferSize")
  override def getCharacterEncoding(): String = fail("No getCharacterEncoding")
  override def getContentType(): String = fail("No getContentType")
  override def getLocale(): Locale = fail("No getLocale")
  override def getWriter(): PrintWriter = fail("No getWriter")
  override def isCommitted(): Boolean = fail("No isCommitted")
  override def reset(): Unit = fail("No reset")
  override def resetBuffer(): Unit = fail("No resetBuffer")
  override def setBufferSize(x: Int): Unit = fail("No setBufferSize")
  override def setCharacterEncoding(x: String): Unit = fail("No setCharacterEncoding")
  override def setContentLength(x: Int): Unit = fail("No setContentLength")
  override def setContentLengthLong(x: Long): Unit = fail("No setContentLengthLong")
  override def setLocale(x: java.util.Locale): Unit = fail("No setLocale")
}
