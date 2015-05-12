package com.socrata.regioncoder

import com.socrata.regioncoder.config.RegionCoderConfig
import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{HandlerList, StatisticsHandler}
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher extends App {
  private val rootPath = "/"

  val config = new RegionCoderConfig(ConfigFactory.load().getConfig("com.socrata"))

  val server = new Server(config.port)
  val context = new WebAppContext()

  context setContextPath rootPath
  context.setResourceBase("src/main/webapp")
  context.addEventListener(new ScalatraListener)
  context.addServlet(classOf[DefaultServlet], rootPath)

  val handlers = new HandlerList
  handlers.addHandler(new StatisticsHandler)
  handlers.addHandler(context)

  server.setHandler(handlers)
  server.setStopTimeout(config.gracefulShutdownMs)
  server.setStopAtShutdown(true)
  server.start()
  server.join()
}
