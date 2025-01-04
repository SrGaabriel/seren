package me.gabriel.seren.logging

import format.{Loggable, LoggableString}
import tracing.NamespacedLogger

import java.time.LocalDateTime

def setupTerminalLogging(): Unit =
  GlobalLoggingContext.targetTerminal()

def log(namespace: String, level: LogLevel, message: String): Unit =
  GlobalLoggingContext.log(
    LoggingContext(
      level,
      Some(namespace),
      LocalDateTime.now()
    ),
    message
  )

def logObject[T](namespace: String, level: LogLevel, message: T)(implicit loggable: Loggable[T]): Unit =
  GlobalLoggingContext.log(
    LoggingContext(
      level,
      Some(namespace),
      LocalDateTime.now()
    ),
    message
  )
  
def createLogger(namespace: String): SerenLogger =
  NamespacedLogger(namespace)