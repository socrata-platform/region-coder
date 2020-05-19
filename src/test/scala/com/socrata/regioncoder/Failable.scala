package com.socrata.regioncoder

trait Failable {
  def fail(s: String): Nothing = throw new Exception(s)
}
