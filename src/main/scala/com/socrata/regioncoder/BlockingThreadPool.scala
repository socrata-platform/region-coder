package com.socrata.regioncoder

import java.util.concurrent.{Executor, Executors, Semaphore}

class BlockingThreadPool(limit: Int) extends Executor {
  private val underlying = Executors.newCachedThreadPool()
  private val semaphore = new Semaphore(limit)

  def execute(r: Runnable): Unit = {
    semaphore.acquire()
    try {
      underlying.execute(new Runnable {
                           override def run() {
                             try {
                               r.run()
                             } finally {
                               semaphore.release()
                             }
                           }
                         })
    } catch {
      case t: Throwable =>
        semaphore.release()
        throw t
    }
  }
}
