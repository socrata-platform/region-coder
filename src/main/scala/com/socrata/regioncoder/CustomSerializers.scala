package com.socrata.regioncoder

import com.rojoma.json.v3.ast.JString
import com.rojoma.json.v3.codec.JsonEncode
import org.locationtech.jts.geom.Envelope

object CustomSerializers {
  implicit val envelopeEncode = new JsonEncode[Envelope] {
    def encode(e: Envelope) = JString(e.toString)
  }
}
