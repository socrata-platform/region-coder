package com.socrata.regioncoder

import com.socrata.geospace.lib.regioncache._
import com.socrata.soda.external.SodaFountainClient
import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{Envelope, Point}
import org.geoscript.geometry.builder
import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.{ThreadPoolExecutor, TimeUnit, Callable, ExecutionException, LinkedBlockingQueue}
import org.slf4j.MDC

trait RegionCoder {
  def cacheConfig: Config
  def concurrencyPerJob: Int
  def partitionXsize: Double
  def partitionYsize: Double
  def sodaFountain: SodaFountainClient

  lazy val spatialCache = new SpatialRegionCache(cacheConfig)
  lazy val stringCache  = new HashMapRegionCache(cacheConfig)

  // Given points, encode them with SpatialIndex and return a sequence of IDs, None if no matching region
  // Points are first encoded into partitions, which are rectangular regions of points
  // Partitions help divide regions into manageable chunks that fit in memory
  protected def regionCodeByPoint[T](resourceName: String,
                                  columnToReturn: String,
                                  points: IndexedSeq[(Double, Double)],
                                  convert: String => T): Seq[Option[T]] = {
    val geoPoints = points.map { case (x, y) => builder.Point(x, y) }
    val partitions = pointsToPartitions(geoPoints)

    val executor = new ThreadPoolExecutor(1, concurrencyPerJob, 1, TimeUnit.SECONDS, new LinkedBlockingQueue)
    try {
      // Map unique partitions to SpatialIndices, fetching them in parallel using Futures
      // Now we have a Seq[Future[Envelope -> SpatialIndex]]
      val callingThreadContext = MDC.getCopyOfContextMap
      val indexFutures = partitions.toSet.map { partEnvelope: Envelope =>
        executor.submit(new Callable[(Envelope, SpatialIndex[String])] {
                          def call() = {
                            MDC.setContextMap(callingThreadContext)
                            partEnvelope -> spatialCache.getFromSoda(sodaFountain, resourceName, columnToReturn, Some(partEnvelope))
                          }
                        })
      }
      // Turn sequence of futures into one Map[Envelope -> SpatialIndex]
      // which will be done when all the indices/partitions have been fetched
      val envToIndex = indexFutures.map(_.get).toMap

      (0 until geoPoints.length).map { i =>
        envToIndex(partitions(i)).firstContains(geoPoints(i)).map { entry => convert(entry.item) }
      }
    } catch {
      case e: ExecutionException =>
        throw e.getCause
    } finally {
      executor.shutdownNow()
    }
  }

  protected def regionCodeByString(resourceName: String,
                                   columnToMatch: String,
                                   columnToReturn: String,
                                   strings: Seq[String]): Seq[Option[Int]] = {
    val index = stringCache.getFromSoda(
      sodaFountain, RegionCacheKey(resourceName, columnToMatch, columnToReturn), columnToReturn)
    strings.map { str => index.get(str.toLowerCase) }
  }

  protected def resetRegionState(): Unit = {
    spatialCache.reset()
    stringCache.reset()
  }

  // Maps each point to its enclosing partition.  The world is divided into evenly spaced partitions
  // according to partitionXYsize.
  protected def pointsToPartitions(points: Seq[Point]): Seq[Envelope] = {
    points.map { point =>
      val partitionX = Math.floor(point.getX / partitionXsize) * partitionXsize
      val partitionY = Math.floor(point.getY / partitionYsize) * partitionYsize
      new Envelope(partitionX, partitionX + partitionXsize,
        partitionY, partitionY + partitionYsize)
    }
  }
}
