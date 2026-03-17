import scala.sys.process.Process

organization := "com.socrata"

name := "region-coder"

scalaVersion := "2.12.21"

Test / fork := true

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest)

resolvers := Seq("Socrata Artifactory" at "https://repo.socrata.com/artifactory/libs-release/")

libraryDependencies ++= Seq(
  "ch.qos.logback"           % "logback-classic"              % "1.1.2",
  "com.socrata"              %% "socrata-http-client"         % "3.16.5-jdk11",
  "com.socrata"              %% "socrata-http-server"         % "3.16.5-jdk11",
  "com.socrata"              %% "socrata-http-curator-broker" % "3.16.5-jdk11",
  "com.socrata"              %% "socrata-http-jetty"          % "3.16.5-jdk11",
  "com.socrata"              %% "socrata-thirdparty-utils"    % "5.1.0",
  "com.socrata"              %% "socrata-curator-utils"       % "1.2.0",
  "com.socrata"              %% "soda-fountain-external"      % "2.1.51", // TODO doesn't have a 2.12 version published in artifactory. check back after 3/17 release build
  "com.socrata"              %% "soql-types"                  % "4.4.2"
    exclude("org.jdom", "jdom")
    exclude("javax.media", "jai_core"),
  "com.typesafe"              % "config"                    % "1.4.3",
  "io.spray"                  % "spray-caching"             % "1.2.2",
  "nl.grons"                 %% "metrics4-scala"            % "4.1.19",
  "org.apache.commons"        % "commons-io"                % "1.3.2",
  "org.apache.commons"        % "commons-collections4"      % "4.4",
  "org.apache.curator"        % "curator-x-discovery"       % "2.4.2"
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("log4j", "log4j"),
  "org.velvia"               %% "geoscript"                 % "0.8.3" // TODO doesn't have a 2.12 version published in artifactory
    exclude("org.geotools", "gt-xml")
    exclude("org.geotools", "gt-render")
    exclude("org.scala-lang", "scala-swing")
    exclude("com.lowagie", "itext")
    exclude("javax.media", "jai_core"),
  "nl.grons"                 %% "metrics4-scala"            % "3.3.0",

  "com.socrata"              %% "socrata-thirdparty-test-utils" % "4.0.15" % "test",
  "com.socrata"              %% "socrata-curator-test-utils"    % "1.1.2" % "test",
  "org.apache.curator"        % "curator-test"                  % "2.4.2" % "test",
  "org.scalatest"            %% "scalatest"                     % "3.2.19" % "test"
)

def gitSha = Process(Seq("git", "describe", "--always", "--dirty", "--long", "--abbrev=10")).!!.stripLineEnd

buildInfoPackage := "com.socrata.regioncoder"

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  scalaVersion,
  sbtVersion,
  BuildInfoKey.action("buildTime") {
    new org.joda.time.DateTime(System.currentTimeMillis).toString()
  },
  BuildInfoKey.action("revision") { gitSha }
)

buildInfoOptions += BuildInfoOption.ToMap

releaseProcess := releaseProcess.value.filterNot(_ == ReleaseTransformations.publishArtifacts)

enablePlugins(sbtbuildinfo.BuildInfoPlugin)

assembly/assemblyJarName := s"${name.value}-assembly.jar"
