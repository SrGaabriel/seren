package me.gabriel.seren.logging

import java.time.LocalDateTime

case class LoggingContext(
  level: LogLevel,
  origin: Option[String],
  time: LocalDateTime
)