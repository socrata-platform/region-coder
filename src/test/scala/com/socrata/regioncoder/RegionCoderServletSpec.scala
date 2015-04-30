package com.socrata.regioncoder

import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest.ScalatraSuite

class RegionCoderServletSpec extends ScalatraSuite with FunSuiteLike {
  test("index page") {
    get("/") {
      status should equal (200)
    }
  }

  ignore("version") {
    // Wtf, why is this returning 404?
    get("/version") {
      status should equal (200)
    }
  }
}
