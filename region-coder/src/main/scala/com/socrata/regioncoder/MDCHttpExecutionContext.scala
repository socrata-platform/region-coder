package com.socrata.regioncoder

import org.slf4j.MDC
import scala.concurrent.{ExecutionContextExecutor, ExecutionContext}

/** *
  * Creates a new MDCHttpExecutionContext from an existing context
  */
object MDCHttpExecutionContext {
  def fromThread(delegate: ExecutionContext): ExecutionContextExecutor =
    new MDCHttpExecutionContext(MDC.getCopyOfContextMap, delegate)
}

/** *
  * A custom execution context that ensures that MDC values are copied to worker threads correctly.
  * Adapted from this blog post:
  * http://yanns.github.io/blog/2014/05/04/slf4j-mapped-diagnostic-context-mdc-with-play-framework/
  */
class MDCHttpExecutionContext(mdcContext: java.util.Map[String, String], delegate: ExecutionContext)
  extends ExecutionContextExecutor {

  def execute(runnable: Runnable): Unit = {
    val callingThreadContext = MDC.getCopyOfContextMap
    delegate.execute(new Runnable {
      def run(): Unit = {
        val originalThreadContext = MDC.getCopyOfContextMap
        setContextMap(callingThreadContext)
        try {
          runnable.run()
        } finally {
          setContextMap(originalThreadContext)
        }
      }
    })
  }

  private[this] def setContextMap(context: java.util.Map[String, String]): Unit =
    Option(context) match {
      case Some(c) => MDC.setContextMap(c)
      case None    => MDC.clear()
    }

  def reportFailure(t: Throwable): Unit = delegate.reportFailure(t)
}

