libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7"
)

externalResolvers ++= Seq(
  "Socrata Artifactory Lib Releases" at "https://repo.socrata.com/artifactory/libs-release/",
  Resolver.url("Socrata Ivy Lib Releases", url("https://repo.socrata.com/artifactory/ivy-libs-release"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("org.scalatra.sbt" % "sbt-scalatra"    % "1.0.4")
addSbtPlugin("com.earldouglas"  % "xsbt-web-plugin" % "4.2.4")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
