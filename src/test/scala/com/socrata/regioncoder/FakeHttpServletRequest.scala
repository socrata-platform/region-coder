package com.socrata.regioncoder

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import javax.servlet.http.HttpServletRequest
import javax.servlet._

class FakeHttpServletRequest(url: String, content: Option[String], method: Option[String]) extends HttpServletRequest with Failable {
  val headers = Map("X-Socrata-RequestId" -> "It's a FAAAAKE")
  val body = content.map { body =>
    new ServletInputStream {
      val data = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))
      override def read() = data.read()
      override def isFinished = data.available == 0
      override def isReady = !isFinished
      override def setReadListener(x: ReadListener) = fail("No setReadListener")
    }
  }

  def getRequestURI(): String =
    url.indexOf('?') match {
      case -1 => url
      case n => url.take(n)
    }

  def getInputStream(): ServletInputStream = body.getOrElse(fail("No body"))

  def getMethod(): String = method.getOrElse(if(content.isEmpty) "GET" else "POST")

  def getQueryString(): String =
    url.indexOf('?') match {
      case -1 => null
      case n => url.drop(n+1)
    }

  def authenticate(x$1: javax.servlet.http.HttpServletResponse): Boolean = fail("No authenticate")
  def changeSessionId(): String = fail("No changeSessionId")
  def getAuthType(): String = fail("No getAuthType")
  def getContextPath(): String = fail("No getContextPath")
  def getCookies(): Array[javax.servlet.http.Cookie] = fail("No getCookies")
  def getDateHeader(x$1: String): Long = fail("No getDateHeader")
  def getHeader(x: String): String = headers(x)
  def getHeaderNames(): java.util.Enumeration[String] = fail("No getHeaderNames")
  def getHeaders(x$1: String): java.util.Enumeration[String] = fail("No getHeaders")
  def getIntHeader(x$1: String): Int = fail("No getIntHeader")
  def getPart(x$1: String): javax.servlet.http.Part = fail("No getPart")
  def getParts(): java.util.Collection[javax.servlet.http.Part] = fail("No getParts")
  def getPathInfo(): String = fail("No getPathInfo")
  def getPathTranslated(): String = fail("No getPathTranslated")
  def getRemoteUser(): String = fail("No getRemoteUser")
  def getRequestURL(): StringBuffer = fail("No getRequestURL")
  def getRequestedSessionId(): String = fail("No getRequestedSessionId")
  def getServletPath(): String = fail("No getServletPath")
  def getSession(): javax.servlet.http.HttpSession = fail("No getSession")
  def getSession(x$1: Boolean): javax.servlet.http.HttpSession = fail("No getSession")
  def getUserPrincipal(): java.security.Principal = fail("No getUserPrincipal")
  def isRequestedSessionIdFromCookie(): Boolean = fail("No isRequestedSessionIdFromCookie")
  def isRequestedSessionIdFromURL(): Boolean = fail("No isRequestedSessionIdFromURL")
  def isRequestedSessionIdFromUrl(): Boolean = fail("No isRequestedSessionIdFromUrl")
  def isRequestedSessionIdValid(): Boolean = fail("No isRequestedSessionIdValid")
  def isUserInRole(x$1: String): Boolean = fail("No isUserInRole")
  def login(x$1: String,x$2: String): Unit = fail("No login")
  def logout(): Unit = fail("No logout")
  def upgrade[T <: javax.servlet.http.HttpUpgradeHandler](x$1: Class[T]): T = fail("No upgrade[T <: javax.servlet.http.HttpUpgradeHandler]")
  
  // Members declared in javax.servlet.ServletRequest
  def getAsyncContext(): javax.servlet.AsyncContext = fail("No getAsyncContext")
  def getAttribute(x$1: String): Object = fail("No getAttribute")
  def getAttributeNames(): java.util.Enumeration[String] = fail("No getAttributeNames")
  def getCharacterEncoding(): String = fail("No getCharacterEncoding")
  def getContentLength(): Int = fail("No getContentLength")
  def getContentLengthLong(): Long = fail("No getContentLengthLong")
  def getContentType(): String = fail("No getContentType")
  def getDispatcherType(): javax.servlet.DispatcherType = fail("No getDispatcherType")
  def getLocalAddr(): String = fail("No getLocalAddr")
  def getLocalName(): String = fail("No getLocalName")
  def getLocalPort(): Int = fail("No getLocalPort")
  def getLocale(): java.util.Locale = fail("No getLocale")
  def getLocales(): java.util.Enumeration[java.util.Locale] = fail("No getLocales")
  def getParameter(x$1: String): String = fail("No getParameter")
  def getParameterMap(): java.util.Map[String,Array[String]] = fail("No getParameterMap")
  def getParameterNames(): java.util.Enumeration[String] = fail("No getParameterNames")
  def getParameterValues(x$1: String): Array[String] = fail("No getParameterValues")
  def getProtocol(): String = fail("No getProtocol")
  def getReader(): java.io.BufferedReader = fail("No getReader")
  def getRealPath(x$1: String): String = fail("No getRealPath")
  def getRemoteAddr(): String = fail("No getRemoteAddr")
  def getRemoteHost(): String = fail("No getRemoteHost")
  def getRemotePort(): Int = fail("No getRemotePort")
  def getRequestDispatcher(x$1: String): javax.servlet.RequestDispatcher = fail("No getRequestDispatcher")
  def getScheme(): String = fail("No getScheme")
  def getServerName(): String = fail("No getServerName")
  def getServerPort(): Int = fail("No getServerPort")
  def getServletContext(): javax.servlet.ServletContext = fail("No getServletContext")
  def isAsyncStarted(): Boolean = fail("No isAsyncStarted")
  def isAsyncSupported(): Boolean = fail("No isAsyncSupported")
  def isSecure(): Boolean = fail("No isSecure")
  def removeAttribute(x$1: String): Unit = fail("No removeAttribute")
  def setAttribute(x$1: String,x$2: Any): Unit = fail("No setAttribute")
  def setCharacterEncoding(x$1: String): Unit = fail("No setCharacterEncoding")
  def startAsync(x$1: javax.servlet.ServletRequest,x$2: javax.servlet.ServletResponse): javax.servlet.AsyncContext = fail("No startAsync")
  def startAsync(): javax.servlet.AsyncContext = fail("No startAsync")
}
