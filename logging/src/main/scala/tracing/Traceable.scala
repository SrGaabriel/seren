package me.gabriel.seren.logging
package tracing

import java.time.LocalDateTime

trait Traceable extends SerenLogger {
   var traceableName: String = getClass.getName

   def makeContext(level: LogLevel): LoggingContext =
      LoggingContext(
         level,
         Some(traceableName),
         LocalDateTime.now()
      )
}
