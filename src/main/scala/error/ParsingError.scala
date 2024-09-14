package me.gabriel.soma
package error

sealed trait ParsingError extends Error {
  val message: String
}

class NotImplementedFeatureError(val feature: String) extends ParsingError {
  override val message: String = s"Feature $feature is not implemented yet"
}