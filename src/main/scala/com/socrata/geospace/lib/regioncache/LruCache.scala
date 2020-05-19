package com.socrata.geospace.lib.regioncache

import scala.collection.JavaConverters._
import org.apache.commons.collections4.map.LRUMap

class LruCache[K, V](maxEntries: Int) {
  private val cache = new LRUMap[K, V](maxEntries)

  def apply(key: K)(f: => V): V = {
    Option(cache.synchronized { cache.get(key) }) match {
      case Some(value) =>
        value
      case None =>
        val value: V = f
        cache.synchronized(cache.put(key, value))
        value
    }
  }

  def remove(key: K): Unit = {
    cache.synchronized { cache.remove(key) }
  }

  def size: Int = {
    cache.synchronized { cache.size }
  }

  def clear(): Unit = {
    cache.synchronized { cache.clear() }
  }

  def entries: Map[K, V] = {
    cache.synchronized {
      cache.mapIterator.asScala.map { key => key -> cache.get(key, false) }.toMap
    }
  }

  def orderedEntries: Seq[(K, V)] = {
    cache.synchronized {
      cache.mapIterator.asScala.map { key => key -> cache.get(key, false) }.toVector
    }
  }
}

object LruCache {
  def apply[K, V](maxEntries: Int) = new LruCache[K, V](maxEntries)
}
