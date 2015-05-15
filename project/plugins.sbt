libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7"
)

resolvers += "socrata maven" at "https://repository-socrata-oss.forge.cloudbees.com/release"

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt"    % "0.4.0")
addSbtPlugin("com.earldouglas"  % "xsbt-web-plugin" % "1.1.0")
addSbtPlugin("com.socrata" % "socrata-sbt-plugins" % "1.4.4")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")
