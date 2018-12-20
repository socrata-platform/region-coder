import org.scalatra.sbt._
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKeys._
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption, BuildInfoPlugin}

object RegionCoderBuild extends Build {
  private val port = SettingKey[Int]("port")
  private val Conf = config("container")
  private val ScalatraVersion = "2.4.0.RC1"
  private val JettyVersion = "8.1.8.v20121106"

  lazy val project = Project (
    "region-coder",
    file("."),
    settings = ScalatraPlugin.scalatraWithJRebel ++ Seq(
      // TODO: enable code coverage minimum
      scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false,
      organization := "com.socrata",
      name := "region-coder",
      scalaVersion := "2.10.5",
      port in Conf := 2021,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "GeoTools" at "http://download.osgeo.org/webdav/geotools/",
      resolvers += "velvia maven" at "http://dl.bintray.com/velvia/maven",
      resolvers += "socrata artifactory" at "https://repo.socrata.com/artifactory/libs-release",
      libraryDependencies ++= scalatraDeps ++ socrataDeps ++ testDeps,
      buildInfoPackage := "com.socrata.regioncoder",
      buildInfoKeys := Seq[BuildInfoKey](
        name,
        version,
        scalaVersion,
        sbtVersion,
        BuildInfoKey.action("buildTime") {
          new org.joda.time.DateTime(System.currentTimeMillis).toString()
        },
        BuildInfoKey.action("revision") { gitSha }),
      buildInfoOptions += BuildInfoOption.ToMap
    )
  ).enablePlugins(BuildInfoPlugin)

  lazy val gitSha = Process(Seq("git", "describe", "--always", "--dirty", "--long", "--abbrev=10")).!!.stripLineEnd

  lazy val scalatraDeps = Seq(
    "ch.qos.logback"    % "logback-classic"   % "1.1.2" % "runtime",
    "javax.servlet"     % "javax.servlet-api" % "3.1.0",
    "org.eclipse.jetty" % "jetty-plus"        % JettyVersion % "container",
    "org.eclipse.jetty" % "jetty-webapp"      % JettyVersion % "container;compile"
      exclude("org.eclipse.jetty.orbit", "javax.servlet"),
    "org.json4s"       %% "json4s-jackson"    % "3.3.0.RC1",
    "org.scalatra"     %% "scalatra"          % ScalatraVersion,
    "org.scalatra"     %% "scalatra-json"     % ScalatraVersion,
    "org.scalatra"     %% "scalatra-metrics"  % ScalatraVersion
  )

  lazy val socrataDeps = Seq(
    "com.socrata"              %% "socrata-http-client"      % "3.11.4",
    "com.socrata"              %% "socrata-thirdparty-utils" % "4.0.15",
    "com.socrata"              %% "socrata-curator-utils" % "1.1.2",
    "com.socrata"              %% "soda-fountain-external"   % "2.1.51",
    "com.socrata"              %% "soql-types"               % "2.11.4"
      exclude("org.jdom", "jdom")
      exclude("javax.media", "jai_core"),
    "com.typesafe"              % "config"                    % "1.0.2",
    "com.typesafe"             %% "scalalogging-slf4j"        % "1.1.0",
    "io.spray"                  % "spray-caching"             % "1.2.2",
    "nl.grons"                 %% "metrics-scala"             % "3.3.0",
    "org.apache.commons"        % "commons-io"                % "1.3.2" % "provided",
    "org.apache.curator"        % "curator-x-discovery"       % "2.4.2"
      exclude("org.slf4j", "slf4j-log4j12")
      exclude("log4j", "log4j"),
    "org.velvia"               %% "geoscript"                 % "0.8.3"
      exclude("org.geotools", "gt-xml")
      exclude("org.geotools", "gt-render")
      exclude("org.scala-lang", "scala-swing")
      exclude("com.lowagie", "itext")
      exclude("javax.media", "jai_core"),
    "nl.grons"                 %% "metrics-scala"            % "3.3.0"
  )

  lazy val testDeps = Seq(
    "com.github.tomakehurst"    % "wiremock"                      % "1.46"  % "test",
    "com.socrata"              %% "socrata-thirdparty-test-utils" % "4.0.15" % "test",
    "com.socrata"              %% "socrata-curator-test-utils"    % "1.1.2" % "test",
    "org.apache.curator"        % "curator-test"                  % "2.4.2" % "test",
    "org.scalatra"             %% "scalatra-scalatest"            % ScalatraVersion % "test"
  )
}
