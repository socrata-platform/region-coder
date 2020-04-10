package com.socrata.regioncoder

import com.socrata.geospace.lib.regioncache._
import com.socrata.soda.external.SodaFountainClient
import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{Envelope, Point}
import org.geoscript.geometry.builder
import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.Executors

trait RegionCoder {
  def cacheConfig: Config
  def partitionXsize: Double
  def partitionYsize: Double
  def sodaFountain: SodaFountainClient

  lazy val spatialCache = new SpatialRegionCache(cacheConfig)
  lazy val stringCache  = new HashMapRegionCache(cacheConfig)
  lazy val labelCache = new LabelCache(cacheConfig)

  protected implicit val executor: ExecutionContext

  protected def regionCodeByTransform(resourceName: String,
                                      featureIdColumn: String,
                                      labelToReturn: Option[String],
                                      points: Seq[Seq[Double]]): Future[Seq[Option[Any]]] = {
    labelToReturn match {
      case Some(_labelToReturn) => regionCodeLabelByPoint(resourceName, featureIdColumn, _labelToReturn, points)
      case _ => regionCodeByPoint(resourceName, featureIdColumn, points)
    }

  }

  // Given points, encode them with SpatialIndex and return a sequence of IDs, None if no matching region
  // Points are first encoded into partitions, which are rectangular regions of points
  // Partitions help divide regions into manageable chunks that fit in memory
  protected def regionCodeByPoint(resourceName: String,
                                  columnToReturn: String,
                                  points: Seq[Seq[Double]]): Future[Seq[Option[Int]]] = {
    val geoPoints = points.map { case Seq(x, y) => builder.Point(x, y) }
    val partitions = pointsToPartitions(geoPoints)
    // Map unique partitions to SpatialIndices, fetching them in parallel using Futures
    // Now we have a Seq[Future[Envelope -> SpatialIndex]]
    val indexFutures = partitions.toSet.map { partEnvelope: Envelope =>
      spatialCache.getFromSoda(sodaFountain, resourceName, columnToReturn, Some(partEnvelope))
        .map(partEnvelope -> _)
    }
    // Turn sequence of futures into one Future[Map[Envelope -> SpatialIndex]]
    // which will be done when all the indices/partitions have been fetched
    Future.sequence(indexFutures).map(_.toMap).map { envToIndex =>
      (0 until geoPoints.length).map { i =>
        envToIndex(partitions(i)).firstContains(geoPoints(i)).map(_.item)
      }
    }
  }

  protected def regionCodeLabelByPoint(resourceName: String,
                                       featureIdColumn: String,
                                       labelToReturn: String,
                                       points: Seq[Seq[Double]]): Future[Seq[Option[Any]]] = {

    val geoPoints = points.map { case Seq(x, y) => builder.Point(x, y) }
    val partitions = pointsToPartitions(geoPoints)
    val indexStringMap = labelCache.constructHashMap(sodaFountain, resourceName, featureIdColumn, labelToReturn)
    // Map unique partitions to SpatialIndices, fetching them in parallel using Futures
    // Now we have a Seq[Future[Envelope -> SpatialIndex]]
    val indexFutures = partitions.toSet.map { partEnvelope: Envelope =>
      spatialCache.getFromSoda(sodaFountain, resourceName, featureIdColumn, Some(partEnvelope))
        .map(partEnvelope -> _)
    }
    // Turn sequence of futures into one Future[Map[Envelope -> SpatialIndex]]
    // which will be done when all the indices/partitions have been fetched
    Future.sequence(indexFutures).map(_.toMap).map { envToIndex =>
      (0 until geoPoints.length).map { i =>
        envToIndex(partitions(i)).firstContains(geoPoints(i)).map(t => indexStringMap.get(t.item))
      }
    }
  }

  protected def regionCodeByString(resourceName: String, columnToMatch: String,
                                   columnToReturn: String, strings: Seq[String]): Future[Seq[Option[Int]]] = {
    val futureIndex: Future[Map[String, Int]] = stringCache.getFromSoda(
      sodaFountain, RegionCacheKey(resourceName, columnToMatch), columnToReturn)
    futureIndex.map { index => strings.map { str => index.get(str.toLowerCase) } }
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
