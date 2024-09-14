package me.gabriel.soma
package error

import struct.TokenKind

sealed trait ParsingError extends Error {
  val message: String
}

object ParsingError {
  case class NotImplementedFeatureError(feature: String) extends ParsingError {
    override val message: String = s"Feature $feature is not implemented yet"
  }

  case class UnexpectedTokenError(token: String) extends ParsingError {
    override val message: String = s"Unexpected token: $token"
  }
  
  case class InvalidBinaryOpError(op: TokenKind) extends ParsingError {
    override val message: String = s"Invalid binary operation: $op"
  }
}