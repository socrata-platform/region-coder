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
      libraryDependencies ++= scalatraDeps ++ socrataDeps ++ testDeps,
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
    "ch.qos.logback"    % "logback-classic"   % "1.1.2" % "runtime",
    "javax.servlet"     % "javax.servlet-api" % "3.1.0",
    "org.eclipse.jetty" % "jetty-plus"        % "8.1.8.v20121106" % "container",
    "org.eclipse.jetty" % "jetty-webapp"      % "8.1.8.v20121106" % "container;compile"
      exclude("org.eclipse.jetty.orbit", "javax.servlet"),
    "org.json4s"       %% "json4s-jackson"    % "3.2.6",
    "org.scalatra"     %% "scalatra"          % ScalatraVersion,
    "org.scalatra"     %% "scalatra-json"     % ScalatraVersion
  )

  lazy val socrataDeps = Seq(
    "com.codahale.metrics"      % "metrics-graphite"         % "3.0.2"
      exclude("com.codahale.metrics", "metrics-core"),
    "com.socrata"              %% "geospace-library"         % "0.4.6",
    "com.socrata"              %% "socrata-http-client"      % "3.1.1",
    "com.socrata"              %% "socrata-thirdparty-utils" % "3.0.0",
    "com.socrata"              %% "soda-fountain-external"   % "0.5.0",
    "io.dropwizard.metrics"     % "metrics-jetty8"           % "3.1.0"
      exclude("org.eclipse.jetty", "jetty-server"),
    "nl.grons"                 %% "metrics-scala"            % "3.3.0"
  )

  lazy val testDeps = Seq(
    "com.github.tomakehurst"    % "wiremock"                      % "1.46"  % "test",
    "com.socrata"              %% "socrata-thirdparty-test-utils" % "3.0.0" % "test",
    "org.apache.curator"        % "curator-test"                  % "2.4.2" % "test",
    "org.scalatest"            %% "scalatest"                     % "2.1.0" % "test",
    "org.scalatra"             %% "scalatra-scalatest"            % ScalatraVersion % "test"
  )
}
