package com.socrata.geospace.lib.regioncache

import com.rojoma.json.v3.ast.{JArray, JObject, JString}
import com.socrata.geospace.lib.client.SodaResponse
import com.socrata.soda.external.SodaFountainClient
import com.socrata.soql.environment.ResourceName
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class LabelCache(config: Config)(implicit executionContext: ExecutionContext)
  extends RegionCache[Map[Int, String]](config) {

  protected def getEntryFromFeatureJson(features: Seq[com.socrata.thirdparty.geojson.FeatureJson],
                                        resourceName: String,
                                        keyAttribute: String,
                                        valueAttribute: String): Map[Int,String] = ???

  protected def getEntryFromFeatures(features: Seq[org.geoscript.feature.Feature],
                                     keyName: String): Map[Int,String] = ???

  /**
   * Generates an in-memory map for the dataset mapping the numeric feature ids to a string label
   * @param sodaFountain the Soda Fountain client
   * @param resourceName the resource name to pull from Soda Fountain and the column to use as the cache entry key
   * @param featureIdColumn name of the column that should be used as the cache entry value
   * @return Map containing the feature ids and labels for a dataset
   */
  def constructHashMap(sodaFountain: SodaFountainClient, resourceName: String,
                       featureIdColumn: String, labelToReturn: String): Map[Int, String] = {
    val sodaResponse = sodaReadTimer.time {
      sodaFountain.query(resourceName,
        Some("json"),
        Iterable(("$query",
          s"select $labelToReturn as _label, $featureIdColumn as _id")))
    }

    val payload = SodaResponse.check(sodaResponse, StatusOK)
    val exception = new RuntimeException(s"dataset $resourceName contains a feature with missing" +
                                         s"$labelToReturn/$featureIdColumn property")
    payload.toOption
      .flatMap { jValue =>
        jValue match {
          case JArray(rows) =>
            val t = rows.collect { case JObject(obj) =>
              (obj.get("_id"), obj.get("_label")) match {
                case (Some(JString(k)), Some(JString(v))) => Map(k.toInt -> v)
                case _ => throw exception
              }
            }
            Option(t)
          case _ => throw exception
        }
      }
      .getOrElse {
        val errMsg = "Could not read JSON from soda fountain: " + payload.get
        if (payload.isFailure) { throw new RuntimeException(errMsg, payload.failed.get) }
        else                   { throw new RuntimeException(errMsg) }
      }.flatten.toMap
  }
}

