package com.socrata.regioncoder

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.{util => ju}
import java.security.Principal

import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpUpgradeHandler, Part, HttpSession, Cookie}
import javax.servlet._

class FakeHttpServletRequest(url: String, content: Option[String], method: Option[String]) extends HttpServletRequest with Failable {
  val headers = Map("X-Socrata-RequestId" -> "It's a FAAAAKE")

  val body = content.map { str =>
    new ServletInputStream {
      val data = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))
      override def read() = data.read()
      override def isFinished = data.available == 0
      override def isReady = !isFinished
      override def setReadListener(x: ReadListener) = fail("No setReadListener")
    }
  }

  override def getRequestURI(): String =
    url.indexOf('?') match {
      case -1 => url
      case n => url.take(n)
    }

  override def getInputStream(): ServletInputStream = body.getOrElse(fail("No body"))

  override def getMethod(): String = method.getOrElse(if(content.isEmpty) "GET" else "POST")

  override def getQueryString(): String =
    url.indexOf('?') match {
      case -1 => null
      case n => url.drop(n+1)
    }

  override def getHeader(x: String): String = headers(x)

  // Members declared in javax.servlet.http.HttpServletRequest
  override def authenticate(x: HttpServletResponse): Boolean = fail("No authenticate")
  override def changeSessionId(): String = fail("No changeSessionId")
  override def getAuthType(): String = fail("No getAuthType")
  override def getContextPath(): String = fail("No getContextPath")
  override def getCookies(): Array[Cookie] = fail("No getCookies")
  override def getDateHeader(x: String): Long = fail("No getDateHeader")
  override def getHeaderNames(): java.util.Enumeration[String] = fail("No getHeaderNames")
  override def getHeaders(x: String): java.util.Enumeration[String] = fail("No getHeaders")
  override def getIntHeader(x: String): Int = fail("No getIntHeader")
  override def getPart(x: String): Part = fail("No getPart")
  override def getParts(): ju.Collection[Part] = fail("No getParts")
  override def getPathInfo(): String = fail("No getPathInfo")
  override def getPathTranslated(): String = fail("No getPathTranslated")
  override def getRemoteUser(): String = fail("No getRemoteUser")
  override def getRequestURL(): StringBuffer = fail("No getRequestURL")
  override def getRequestedSessionId(): String = fail("No getRequestedSessionId")
  override def getServletPath(): String = fail("No getServletPath")
  override def getSession(): HttpSession = fail("No getSession")
  override def getSession(x: Boolean): HttpSession = fail("No getSession")
  override def getUserPrincipal(): Principal = fail("No getUserPrincipal")
  override def isRequestedSessionIdFromCookie(): Boolean = fail("No isRequestedSessionIdFromCookie")
  override def isRequestedSessionIdFromURL(): Boolean = fail("No isRequestedSessionIdFromURL")
  override def isRequestedSessionIdFromUrl(): Boolean = fail("No isRequestedSessionIdFromUrl")
  override def isRequestedSessionIdValid(): Boolean = fail("No isRequestedSessionIdValid")
  override def isUserInRole(x: String): Boolean = fail("No isUserInRole")
  override def login(x: String, y: String): Unit = fail("No login")
  override def logout(): Unit = fail("No logout")
  override def upgrade[T <: HttpUpgradeHandler](x: Class[T]): T = fail("No upgrade[T <: HttpUpgradeHandler]")

  // Members declared in javax.servlet.ServletRequest
  override def getAsyncContext(): AsyncContext = fail("No getAsyncContext")
  override def getAttribute(x: String): Object = fail("No getAttribute")
  override def getAttributeNames(): ju.Enumeration[String] = fail("No getAttributeNames")
  override def getCharacterEncoding(): String = fail("No getCharacterEncoding")
  override def getContentLength(): Int = fail("No getContentLength")
  override def getContentLengthLong(): Long = fail("No getContentLengthLong")
  override def getContentType(): String = fail("No getContentType")
  override def getDispatcherType(): DispatcherType = fail("No getDispatcherType")
  override def getLocalAddr(): String = fail("No getLocalAddr")
  override def getLocalName(): String = fail("No getLocalName")
  override def getLocalPort(): Int = fail("No getLocalPort")
  override def getLocale(): ju.Locale = fail("No getLocale")
  override def getLocales(): ju.Enumeration[ju.Locale] = fail("No getLocales")
  override def getParameter(x: String): String = fail("No getParameter")
  override def getParameterMap(): ju.Map[String,Array[String]] = fail("No getParameterMap")
  override def getParameterNames(): ju.Enumeration[String] = fail("No getParameterNames")
  override def getParameterValues(x: String): Array[String] = fail("No getParameterValues")
  override def getProtocol(): String = fail("No getProtocol")
  override def getReader(): java.io.BufferedReader = fail("No getReader")
  override def getRealPath(x: String): String = fail("No getRealPath")
  override def getRemoteAddr(): String = fail("No getRemoteAddr")
  override def getRemoteHost(): String = fail("No getRemoteHost")
  override def getRemotePort(): Int = fail("No getRemotePort")
  override def getRequestDispatcher(x: String): RequestDispatcher = fail("No getRequestDispatcher")
  override def getScheme(): String = fail("No getScheme")
  override def getServerName(): String = fail("No getServerName")
  override def getServerPort(): Int = fail("No getServerPort")
  override def getServletContext(): ServletContext = fail("No getServletContext")
  override def isAsyncStarted(): Boolean = fail("No isAsyncStarted")
  override def isAsyncSupported(): Boolean = fail("No isAsyncSupported")
  override def isSecure(): Boolean = fail("No isSecure")
  override def removeAttribute(x: String): Unit = fail("No removeAttribute")
  override def setAttribute(x: String, y: Any): Unit = fail("No setAttribute")
  override def setCharacterEncoding(x: String): Unit = fail("No setCharacterEncoding")
  override def startAsync(x: ServletRequest, y: ServletResponse): AsyncContext = fail("No startAsync")
  override def startAsync(): AsyncContext = fail("No startAsync")
}
