package com.socrata.geospace.lib.regioncache

import com.socrata.geospace.lib.client.SodaResponse
import com.socrata.soda.external.SodaFountainClient
import com.socrata.thirdparty.geojson.{FeatureCollectionJson, FeatureJson, GeoJson}
import com.socrata.thirdparty.metrics.Metrics
import com.typesafe.config.Config
import com.rojoma.json.v3.util.{AutomaticJsonEncodeBuilder, NullForNone}
import com.vividsolutions.jts.geom.{Coordinate, Envelope, GeometryFactory, Polygon}
import com.vividsolutions.jts.io.WKTWriter
import org.geoscript.feature._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext

/**
  * Represents the key for a region cache (dataset resource name + column name)
  * @param resourceName Resource name of the dataset represented in the cache entry
  * @param columnName   Name of the column used as a key for individual features inside the cache entry
  * @param envelope All geometries must be within or intersect with this envelope/bounding box
  */
@NullForNone
case class RegionCacheKey(resourceName: String, columnName: String, columnToReturn: String, envelope: Option[Envelope] = None)
object RegionCacheKey {
  import com.socrata.regioncoder.CustomSerializers._
  implicit val jEncode = AutomaticJsonEncodeBuilder[RegionCacheKey]
}

/**
  * The RegionCache caches indices of the region datasets for geo-region-coding.
  * The cache is populated either from an existing Layer/FeatureCollection in memory, or
  * from soda-fountain dataset.
  *
  * It uses spray-caching for an LRU Cache with a Future API which is thread-safe.
  * If multiple parties try to access the same region dataset, it will not pull from soda-fountain
  * more than once.  The first party will do the expensive pull, while the other parties will just
  * get the Future which is completed when the first party's pull finishes.
  *
  * When a new layer/region dataset is added, the cache will automatically free up existing cached
  * regions as needed to make room for the new one.  The below parameters control that process.
  *
  * @param maxEntries          Maximum capacity of the region cache
  * @tparam T                  Cache entry type
  */
abstract class RegionCache[T](maxEntries: Int = 100)  //scalastyle:ignore
  extends Metrics {
  private val logger = LoggerFactory.getLogger(classOf[RegionCache[_]])

  // To be the same value as HttpStatus.SC_OK
  val StatusOK = 200

  private val GaugeNumEntries = "num-entries"

  def this(config: Config)(implicit executionContext: ExecutionContext) = this(config.getInt("max-entries"))

  protected val cache = LruCache[RegionCacheKey, T](maxEntries)
  def cacheStats = cache.stats

  logger.info("Creating RegionCache with {} entries", maxEntries.toString())

  // There's a bug in the scala-metrics library - metrics.gauge doesn't check if
  // the metric already exists before trying to registering it again.
  // Because of this, unit tests fail unless we check for existence and skip the gauge.
  metrics.registry.synchronized {
    if (!metrics.registry.getNames.contains(s"${getClass.getCanonicalName}.$GaugeNumEntries")) {
      if (!metrics.registry.getNames.contains(GaugeNumEntries)) {
        metrics.gauge(GaugeNumEntries) {
          cache.size
        }
      }
    }
  }

  val sodaReadTimer  = metrics.timer("soda-region-read")
  val regionIndexLoadTimer = metrics.timer("region-index-load")

  /**
    * Generates a cache entry for the dataset given a sequence of features
    * @param features Features from which to generate a cache entry
    * @param keyName  Name of the field on which to index the dataset features
    * @return Cache entry containing the dataset features
    */
  protected def getEntryFromFeatures(features: Seq[Feature], keyName: String): T

  /**
    * Generates a cache entry for the dataset given feature JSON
    * @param features  Feature JSON from which to generate a cache entry
    * @param keyAttribute   Name of the feature attribute to use as the cache entry key
    * @param valueAttribute Name of the feature attribute to use as the cache entry value
    * @return Cache entry containing the dataset features
    */
  protected def getEntryFromFeatureJson(features: Seq[FeatureJson],
                                        resourceName: String,
                                        keyAttribute: String,
                                        valueAttribute: String): T

  /**
    * Any activities that should be carried out before caching a region
    */
  protected def prepForCaching(): Unit = { }

  /**
    * gets an entry from the cache, populating it from a list of features if it's missing
    *
    * @param key the resource name/column name used to cache the entry
    * @param features a Seq of Features to use to create the cache entry if it doesn't exist
    * @return a Future which will hold the cache entry object when populated
    *         Note that if this fails, then it will return a Failure, and can be processed further with
    *         onFailure(...) etc.
    */
  def getFromFeatures(key: RegionCacheKey, features: Seq[Feature]): T = {
    cache(key) {
      logger.info("Populating cache entry for res [{}], col [{}] from features", key.resourceName: Any, key.columnName)
      prepForCaching()
      getEntryFromFeatures(features, key.columnName)
    }
  }

  private def getQueryString(key: RegionCacheKey): String = {
    val where = key.envelope.map { env =>
      // Factories and writers are not thread safe, and this is not perf-sensitive
      val factory = new GeometryFactory
      val writer = new WKTWriter
      // wkt 'MULTIPOLYGON EMPTY'
      lazy val emptyPolygons = {
        Array(factory.createPolygon(Array.empty[Coordinate]))
      }
      val polys = try {
        Array(factory.toGeometry(env).asInstanceOf[Polygon])
      } catch {
        case ex: ClassCastException =>
          // When a number is too large like 1e30, the polygon is reduced to a point.
          // Replace the point with an empty polygon
          emptyPolygons
      }
      // soda fountain only accepts MULTIPOLYGONs not POLYGONs
      val envelopePolyWkt = writer.write(factory.createMultiPolygon(polys))
      s"where intersects(${key.columnName}, '$envelopePolyWkt')"
    }.getOrElse("")
    s"select * $where limit ${Long.MaxValue}"
  }

  /**
    * Gets an entry from the cache, populating it from Soda Fountain as needed
    *
    * @param sodaFountain the Soda Fountain client
    * @param key the resource name to pull from Soda Fountain and the column to use as the cache entry key
    * @param valueColumnName name of the column that should be used as the cache entry value
    */
  def getFromSoda(sodaFountain: SodaFountainClient, key: RegionCacheKey, valueColumnName: String): T =
    cache(key) {
      logger.info("Populating cache entry for resource [{}], column [] from soda fountain client", key.resourceName)
      key.envelope.foreach { env => logger.info("  for envelope {}", env) }

      prepForCaching()
      // Ok, get a Try[JValue] for the response, then parse it using GeoJSON parser
      val query = getQueryString(key)
      val sodaResponse = sodaReadTimer.time {
        sodaFountain.query(key.resourceName, Some("geojson"), Iterable(("$query", query)))
      }
      // Originally using javax lib for this one status code, I doubt highly it will ever change, and
      // we will avoid having to make an import for single item by statically adding it.
      val payload = SodaResponse.check(sodaResponse, StatusOK)
      regionIndexLoadTimer.time {
        payload.toOption.
          flatMap {  jvalue => GeoJson.codec.decode(jvalue).right.toOption }.
          collect { case FeatureCollectionJson(features, _) =>
            getEntryFromFeatureJson(features, key.resourceName, key.columnName, valueColumnName)
          }.
          getOrElse {
            val errMsg = "Could not read GeoJSON from soda fountain: " + payload.get
            if (payload.isFailure) { throw new RuntimeException(errMsg, payload.failed.get) }
            else                   { throw new RuntimeException(errMsg) }
          }
      }
    }

  /**
    * Clears the cache of all entries.  Mostly used for testing.
    */
  def reset(): Unit = { cache.clear() }
}
