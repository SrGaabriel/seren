package me.gabriel.seren.logging
package dispatcher

object NullOutputStream extends java.io.OutputStream {
  override def write(b: Int): Unit = ()
}
