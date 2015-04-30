package com.socrata.regioncoder

class RegionCoderServlet extends RegionCoderStack {
  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  get("/version") {
    Map("version" -> BuildInfo.version,
      "scalaVersion" -> BuildInfo.scalaVersion,
      "dependencies" -> BuildInfo.libraryDependencies,
      "buildTime" -> new org.joda.time.DateTime(BuildInfo.buildTime).toString())
  }
}
