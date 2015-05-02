package com.socrata.regioncoder

import com.socrata.regioncoder.config.RegionCoderConfig
import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.eclipse.jetty.servlet.DefaultServlet
import com.codahale.metrics.jetty8.InstrumentedHandler
import com.socrata.thirdparty.metrics.Metrics

object JettyLauncher extends App {
  private val rootPath = "/"

  val config = new RegionCoderConfig(ConfigFactory.load().getConfig("com.socrata"))

  val server = new Server(config.port)
  val context = new WebAppContext()

  context setContextPath(rootPath)
  context.setResourceBase("src/main/webapp")
  context.addEventListener(new ScalatraListener)
  context.addServlet(classOf[DefaultServlet], rootPath)

  val handler = new InstrumentedHandler(Metrics.metricsRegistry, context, config.metrics.prefix)
  server.setHandler(handler)
  server.setGracefulShutdown(config.gracefulShutdownMs)
  server.setStopAtShutdown(true)
  server.start()
  server.join()

}
