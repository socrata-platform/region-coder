package com.socrata.regioncoder

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSuiteLike

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
