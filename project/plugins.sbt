libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7"
)

externelResolvers ++= Seq(
  "Socrata Artifactory Lib Releases" at "https://repo.socrata.com/artifactory/libs-release/",
  Resolver.url("Socrata Ivy Lib Releases", url("https://repo.socrata.com/artifactory/ivy-libs-release"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt"    % "0.4.0")
addSbtPlugin("com.earldouglas"  % "xsbt-web-plugin" % "1.1.0")
addSbtPlugin("com.socrata" % "socrata-sbt-plugins" % "1.6.8")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")
