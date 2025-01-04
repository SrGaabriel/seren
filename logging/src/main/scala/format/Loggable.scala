package me.gabriel.seren.logging
package format

import java.io.OutputStream

trait Loggable[T] {
  def writeTo(t: T, stream: OutputStream): Unit
}

implicit class LoggableOps[T](t: T) {
  def log(implicit loggable: Loggable[T], stream: OutputStream): Unit =
    loggable.writeTo(t, stream)
}

implicit object LoggableString extends Loggable[String] {
  override def writeTo(t: String, stream: OutputStream): Unit =
    stream.write(t.getBytes)
}

implicit object LoggableInt extends Loggable[Int] {
  override def writeTo(t: Int, stream: OutputStream): Unit =
    stream.write(t.toString.getBytes)
}

implicit object LoggableException extends Loggable[Throwable] {
  override def writeTo(t: Throwable, stream: OutputStream): Unit =
    stream.write(t.getMessage.getBytes)
    t.printStackTrace(new java.io.PrintStream(stream))
}