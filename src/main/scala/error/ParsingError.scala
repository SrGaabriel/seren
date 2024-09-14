package me.gabriel.seren
package error

import struct.Token

sealed trait ParsingError extends Error {
  val message: String
  val token: Token
}

object ParsingError {
  case class UnexpectedTokenError(token: Token) extends ParsingError {
    override val message: String = s"Unexpected token: $token"
  }

  case class InvalidBinaryOpError(token: Token) extends ParsingError {
    override val message: String = s"Invalid binary operation: ${token.kind}"
  }

  case class InvalidIdentifierError(token: Token) extends ParsingError {
    override val message: String = s"Invalid identifier: ${token.value}"
  }

  case class UnexpectedIdentifierOpError(token: Token) extends ParsingError {
    override val message: String = s"Unexpected identifier operation: ${token.kind}"
  }

  case class UnterminatedSequenceError(token: Token) extends ParsingError {
    override val message: String = s"Unterminated sequence: ${token.kind}"
  }
}