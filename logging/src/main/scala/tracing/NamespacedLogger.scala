package me.gabriel.seren.logging
package tracing

import java.time.LocalDateTime

class NamespacedLogger(
  namespace: String,
) extends SerenLogger {
  override def makeContext(level: LogLevel): LoggingContext =
    LoggingContext(
      level,
      Some(namespace),
      LocalDateTime.now()
    )
}
