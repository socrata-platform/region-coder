organization := "com.socrata"

name := "region-coder"

scalaVersion := "2.10.7"

val port = SettingKey[Int]("port")
val Conf = config("container")
val ScalatraVersion = "2.4.0.RC1"
val JettyVersion = "8.1.8.v20121106"

port in Conf := 2021

fork in Test := true

resolvers := Seq("Socrata Artifactory" at "https://repo.socrata.com/artifactory/libs-release/")

libraryDependencies ++= Seq(
  "com.socrata"              %% "socrata-http-client"      % "3.11.4",
  "com.socrata"              %% "socrata-http-server"      % "3.11.4",
  "com.socrata"              %% "socrata-http-jetty"       % "3.11.4",
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
  "org.apache.commons"        % "commons-io"                % "1.3.2",
  "org.apache.commons"        % "commons-collections4"      % "4.4",
  "org.apache.curator"        % "curator-x-discovery"       % "2.4.2"
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("log4j", "log4j"),
  "org.velvia"               %% "geoscript"                 % "0.8.3"
    exclude("org.geotools", "gt-xml")
    exclude("org.geotools", "gt-render")
    exclude("org.scala-lang", "scala-swing")
    exclude("com.lowagie", "itext")
    exclude("javax.media", "jai_core"),
  "nl.grons"                 %% "metrics-scala"            % "3.3.0",

  "com.socrata"              %% "socrata-thirdparty-test-utils" % "4.0.15" % "test",
  "com.socrata"              %% "socrata-curator-test-utils"    % "1.1.2" % "test",
  "org.apache.curator"        % "curator-test"                  % "2.4.2" % "test"
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
