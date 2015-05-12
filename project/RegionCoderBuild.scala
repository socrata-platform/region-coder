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
      libraryDependencies ++= scalatraDeps ++ socrataDeps ++ testDeps,
      buildInfoPackage := "com.socrata.regioncoder",
      buildInfoKeys := Seq[BuildInfoKey](
        name,
        version,
        scalaVersion,
        sbtVersion,
        BuildInfoKey.action("buildTime") { System.currentTimeMillis },
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
    "com.socrata"              %% "geospace-library"         % "0.4.6",
    "com.socrata"              %% "socrata-http-client"      % "3.1.1",
    "com.socrata"              %% "socrata-thirdparty-utils" % "3.0.0",
    "com.socrata"              %% "soda-fountain-external"   % "0.5.0",
    "nl.grons"                 %% "metrics-scala"            % "3.3.0"
  )

  lazy val testDeps = Seq(
    "com.github.tomakehurst"    % "wiremock"                      % "1.46"  % "test",
    "com.socrata"              %% "socrata-thirdparty-test-utils" % "3.0.0" % "test",
    "org.apache.curator"        % "curator-test"                  % "2.4.2" % "test",
    "org.scalatra"             %% "scalatra-scalatest"            % ScalatraVersion % "test"
  )
}
