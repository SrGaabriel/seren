package me.gabriel.seren.logging
package format

import effect.ConsoleColors

import java.time.format.DateTimeFormatter

class ColoredTracingLogFormatter extends LogFormatter {
  var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  var currentBiggestIndentation: Int = 0
  var alignmentThreshold: Int = 8

  override def format(context: LoggingContext): String =
    val levelColor = context.level match
      case LogLevel.INFO => ColoredTracingLogFormatter.infoColor
      case LogLevel.DEBUG => ColoredTracingLogFormatter.debugColor
      case LogLevel.ERROR => ColoredTracingLogFormatter.errorColor
      case LogLevel.WARNING => ColoredTracingLogFormatter.warningColor
      case LogLevel.TRACE => ColoredTracingLogFormatter.traceColor

    val dateTime = context.time.format(dateTimeFormatter)
    val origin = formatOrigin(context.origin.getOrElse("?????"))

    val format = s"${ConsoleColors.colorRGB(114,114,114)}$dateTime ${ConsoleColors.BOLD}[$origin] %s${ConsoleColors.RESET}$levelColor${context.level} ${ConsoleColors.RESET}"
    val padding = if currentBiggestIndentation > format.length && currentBiggestIndentation - format.length < alignmentThreshold then
      " " * (currentBiggestIndentation - format.length)
    else
      currentBiggestIndentation = format.length
      ""
    format.format(padding)

  def formatOrigin(origin: String): String =
    val parts = origin.split("\\.")
    if parts.length > 4 then
      parts.drop(2).mkString(".")
    else
      origin
}

object ColoredTracingLogFormatter {
  var infoColor = ConsoleColors.colorRGB(87, 143, 235)
  var debugColor = ConsoleColors.colorRGB(0, 255, 0)
  var errorColor = ConsoleColors.colorRGB(240, 86, 55)
  var warningColor = ConsoleColors.colorRGB(255, 255, 0)
  var traceColor = ConsoleColors.colorRGB(255, 0, 255)
}