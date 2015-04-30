import sbt._
import sbt.Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import sbtbuildinfo.Plugin._

object RegionCoderBuild extends Build {
  private val port = SettingKey[Int]("port")
  private val Conf = config("container")
  private val ScalatraVersion = "2.2.2"

  lazy val project = Project (
    "region-coder",
    file("."),
    settings = ScalatraPlugin.scalatraWithJRebel ++ buildInfoSettings ++ Seq(
      organization := "com.socrata",
      name := "region-coder",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.10.4",
      port in Conf := 2021,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= scalatraDeps ++ socrataDeps,
      sourceGenerators in Compile <+= buildInfo,
      buildInfoPackage := "com.socrata.regioncoder",
      buildInfoKeys := Seq[BuildInfoKey](
        name,
        version,
        scalaVersion,
        libraryDependencies in Compile,
        BuildInfoKey.action("buildTime") { System.currentTimeMillis }
      )
    )
  )

  lazy val scalatraDeps = Seq(
    "ch.qos.logback"    % "logback-classic"    % "1.1.2" % "runtime",
    "javax.servlet"     % "javax.servlet-api"  % "3.1.0",
    "org.eclipse.jetty" % "jetty-plus"         % "9.1.5.v20140505" % "container",
    "org.eclipse.jetty" % "jetty-webapp"       % "9.1.5.v20140505" % "container",
    "org.json4s"       %% "json4s-jackson"     % "3.2.6",
    "org.scalatra"     %% "scalatra"           % ScalatraVersion,
    "org.scalatra"     %% "scalatra-json"      % ScalatraVersion,
    "org.scalatra"     %% "scalatra-scalatest" % ScalatraVersion % "test"
  )

  lazy val socrataDeps = Seq(
    "com.socrata"              %% "geospace-library"          % "0.4.6",
    "com.socrata"              %% "socrata-http-client"       % "3.1.1",
    "com.socrata"              %% "socrata-thirdparty-utils"  % "3.0.0",
    "com.socrata"              %% "soda-fountain-external"    % "0.5.0",
    "nl.grons"                 %% "metrics-scala"             % "3.3.0"
  )
}