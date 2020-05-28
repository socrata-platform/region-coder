package com.socrata.regioncoder

import com.socrata.geospace.lib.regioncache._
import com.socrata.soda.external.SodaFountainClient
import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{Envelope, Point}
import org.geoscript.geometry.builder
import java.util.concurrent.{ThreadPoolExecutor, TimeUnit, Callable, ExecutionException, LinkedBlockingQueue, Future, CompletableFuture}
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

    val executor = new ThreadPoolExecutor(concurrencyPerJob, concurrencyPerJob, 1, TimeUnit.SECONDS, new LinkedBlockingQueue)
    try {
      // Map unique partitions to SpatialIndices, fetching them in parallel using Futures
      // Now we have a Seq[Future[Envelope -> SpatialIndex]]
      val callingThreadContext = Option(MDC.getCopyOfContextMap)
      type IndexResult = Option[(Envelope, SpatialIndex[String])]
      val indexFutures: Seq[Future[IndexResult]] =
        partitions.distinct.map { maybePartEnvelope: Option[Envelope] =>
          maybePartEnvelope match {
            case Some(partEnvelope) =>
              executor.submit(new Callable[IndexResult] {
                                def call() = {
                                  callingThreadContext.foreach(MDC.setContextMap)
                                  Some(partEnvelope -> spatialCache.getFromSoda(sodaFountain, resourceName, columnToReturn, Some(partEnvelope)))
                                }
                              })
            case None =>
              CompletableFuture.completedFuture[IndexResult](None)
          }
        }
      // Turn the sequence of futures into one Map[Envelope -> SpatialIndex]
      // which will be done when all the indices/partitions have been fetched
      val envToIndex = indexFutures.flatMap(_.get).toMap

      (0 until geoPoints.length).map { i =>
        for {
          partition <- partitions(i)
          entry <- envToIndex(partition).firstContains(geoPoints(i))
        } yield {
          convert(entry.item)
        }
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

  def isReasonable(point: Point) = {
    // probably 360 is over-generous, but I can see 180 going wrong
    // sometimes so we'll allow the full circle.
    point.getX.abs <= 360 && point.getY.abs <= 90
  }

  // Maps each point to its enclosing partition.  The world is divided into evenly spaced partitions
  // according to partitionXYsize.
  protected def pointsToPartitions(points: Seq[Point]): Seq[Option[Envelope]] = {
    points.map { point =>
      if(isReasonable(point)) {
        val partitionX = Math.floor(point.getX / partitionXsize) * partitionXsize
        val partitionY = Math.floor(point.getY / partitionYsize) * partitionYsize
        Some(new Envelope(partitionX, partitionX + partitionXsize,
                          partitionY, partitionY + partitionYsize))
      } else {
        None
      }
    }
  }
}
