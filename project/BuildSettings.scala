import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys

object BuildSettings {
  def buildSettings: Seq[Setting[_]] =
    Defaults.itSettings
//  ++ Seq(
//      com.socrata.sbtplugins.StylePlugin.StyleKeys.styleCheck in Test := {},
//      com.socrata.sbtplugins.StylePlugin.StyleKeys.styleCheck in Compile := {},
//      scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false,
//      scalaVersion := "2.11.8",
//      resolvers ++= Seq(
//        "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools",
//        "socrata maven" at "https://repo.socrata.com/artifactory/libs-release/",
//        "socrata snapshot" at "https://repo.socrata.com/artifactory/libs-snapshot/",
//        "socrata releases" at "https://repository-socrata-oss.forge.cloudbees.com/release"),
//      scalacOptions ++= Seq("-deprecation", "-feature")
//    )

  def projectSettings(assembly: Boolean = false): Seq[Setting[_]] =
    buildSettings ++
      (if (!assembly) Seq(AssemblyKeys.assembly := file(".")) else Nil)
}
