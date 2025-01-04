package me.gabriel.seren.logging

import format.{Loggable, LoggableException, LoggableString}

trait SerenLogger {
  def logObject[T](level: LogLevel, message: T)(implicit loggable: Loggable[T]): Unit =
    GlobalLoggingContext.log(
      this.makeContext(level),
      message
    )
  
  def lazyLogObject[T](level: LogLevel, message: T)(implicit loggable: Loggable[T]): Unit =
    GlobalLoggingContext.lazyLog(
      this.makeContext(level),
      message
    )
  
  def lazyLogObject[T](level: LogLevel, message: () => T)(implicit loggable: Loggable[T]): Unit =
    GlobalLoggingContext.lazyLog(
      this.makeContext(level),
      message
    )
  
  def makeContext(
    level: LogLevel
  ): LoggingContext
  
  def log(level: LogLevel, message: String): Unit = 
    this.logObject(level, message)(LoggableString)
    
  def log(level: LogLevel, throwable: Throwable): Unit = 
    this.logObject(level, throwable)(LoggableException)

  def lazyLog(level: LogLevel, message: String): Unit =
    this.lazyLogObject(level, message)(LoggableString)

  def lazyLog(level: LogLevel, message: () => String): Unit =
    this.lazyLogObject(level, message)(LoggableString)
    
  def dispatchAllQueuedLogs(): Unit = GlobalLoggingContext.dispatchAllQueuedLogs()
}
