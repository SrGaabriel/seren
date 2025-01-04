package me.gabriel.seren.logging
package format

trait LogFormatter {
  def format(
    context: LoggingContext
  ): String
}
