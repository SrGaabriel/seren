package me.gabriel.seren.logging
package dispatcher

import format.{Loggable, LoggableString}

import java.io.OutputStream

class LogDispatcher(outputStream: OutputStream) {
  private val logQueue = new scala.collection.mutable.Queue[(Loggable[?], () => ?)]()
  
  def autoDispatch[T](message: T)(implicit writer: Loggable[T]) : Unit =
    writer.writeTo(message, outputStream)

  def lazyAutoDispatch[T](message: T)(implicit writer: Loggable[T]): Unit =
    logQueue.enqueue((writer, () => message))

  def dispatch[T](writer: Loggable[T], message: T) : Unit =
    writer.writeTo(message, outputStream)

  def lazyDispatch[T](writer: Loggable[T], message: () => T) : Unit =
    logQueue.enqueue((writer, message))

  def newline(): Unit =
    outputStream.write("\n".getBytes)

  def enqueueNewline(): Unit =
    logQueue.enqueue((LoggableString, () => "\n"))

  def dumpAll() : Unit =
    while (logQueue.nonEmpty) {
      val (writer, message) = logQueue.dequeue()
      dispatch[Any](writer.asInstanceOf[Loggable[Any]], message())
    }
}
