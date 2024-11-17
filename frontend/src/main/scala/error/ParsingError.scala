package me.gabriel.seren.frontend
package error

import struct.{Token, TokenKind}

sealed trait ParsingError extends Error {
  val message: String
  val token: Token
}

object ParsingError {
  case class UnexpectedTokenError(token: Token) extends ParsingError {
    override val message: String = s"Unexpected token: $token"
  }
  
  class ExpectedTokenError(val token: Token, val expected: TokenKind) extends ParsingError {
    override val message: String = s"Expected $expected, found $token"
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
  
  case class InvalidTypeDeclarationError(token: Token) extends ParsingError {
    override val message: String = s"Invalid type declaration: ${token.kind}(${token.value})"
  }
}