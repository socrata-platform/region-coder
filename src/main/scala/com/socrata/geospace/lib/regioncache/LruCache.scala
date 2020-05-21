package com.socrata.geospace.lib.regioncache

import scala.collection.JavaConverters._
import scala.collection.mutable

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.apache.commons.collections4.map.LRUMap

class LruCache[K, V](maxEntries: Int) {
  private class Waiter {
    var count = 1
  }

  private val cache = new LRUMap[K, V](maxEntries)
  private val waiters = new mutable.HashMap[K, Waiter]

  def apply(key: K)(f: => V): V = {
    Option(cache.synchronized { cache.get(key) }) match {
      case Some(value) =>
        value

      case None =>
        // it wasn't there; we'll want to compute it, but only if no
        // one else is trying to compute it at the same time.  We'll
        // create a lock-per-key to adjudicate that.
        val waiter =
          waiters.synchronized {
            val w = waiters.getOrElseUpdate(key, new Waiter)
            w.count += 1
            w
          }

        try {
          waiter.synchronized {
            Option(cache.synchronized { cache.get(key) }) match {
              case Some(value) =>
                // someone else got there first
                value

              case None =>
                // no one got there first, so finally we actually
                // compute the value to cache.
                val value: V = f
                cache.synchronized { cache.put(key, value) }
                value
            }
          }
        } finally {
          waiters.synchronized {
            waiter.count -= 1
            if(waiter.count == 0) waiters.remove(key)
          }
        }
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
