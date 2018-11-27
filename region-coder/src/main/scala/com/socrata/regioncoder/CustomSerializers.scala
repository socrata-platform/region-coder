package com.socrata.regioncoder

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JString, JNull}
import com.vividsolutions.jts.geom.Envelope

class NoneSerializer extends CustomSerializer[Option[_]] ( format => (
  { case JNull => None },
  { case None => JNull }
  ))

class EnvelopeSerializer extends CustomSerializer[Envelope] ( format => (
  Map.empty,
  { case e: Envelope => JString(e.toString) }
  ))
