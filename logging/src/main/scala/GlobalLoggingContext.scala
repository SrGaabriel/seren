package me.gabriel.seren.logging

import dispatcher.{LogDispatcher, NullOutputStream}
import format.{ColoredTracingLogFormatter, LogFormatter, Loggable, LoggableString}

import java.io.{FileOutputStream, OutputStream}

object GlobalLoggingContext {
  var logFormatter: LogFormatter = ColoredTracingLogFormatter()
  var logDispatcher: LogDispatcher = new LogDispatcher(NullOutputStream)

  def log[T](context: LoggingContext, message: T)(implicit loggable: Loggable[T]): Unit =
    logDispatcher.autoDispatch(logFormatter.format(context))
    logDispatcher.dispatch(loggable, message)
    logDispatcher.newline()

  def lazyLog[T](context: LoggingContext, message: T)(implicit loggable: Loggable[T]): Unit =
    logDispatcher.autoDispatch(logFormatter.format(context))
    logDispatcher.lazyDispatch(loggable, () => message)
    logDispatcher.enqueueNewline()
  
  def lazyLog[T](context: LoggingContext, message: () => T)(implicit loggable: Loggable[T]): Unit =
    logDispatcher.autoDispatch(logFormatter.format(context))
    logDispatcher.lazyDispatch(loggable, message)
    logDispatcher.enqueueNewline()

  def setOutput(outputStream: OutputStream): Unit =
    logDispatcher = new LogDispatcher(outputStream)

  def targetTerminal(): Unit =
    setOutput(System.out)

  def targetFile(filePath: String): Unit =
    setOutput(new FileOutputStream(filePath))

  def targetFile(file: java.io.File): Unit =
    setOutput(new FileOutputStream(file))

  def dispatchAllQueuedLogs(): Unit =
    logDispatcher.dumpAll()
}
