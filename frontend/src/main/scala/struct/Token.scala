package me.gabriel.seren.frontend
package struct

case class Token(value: String, kind: TokenKind)

enum TokenKind:
  case BOF
  case Number
  case Plus
  case Minus
  case String
  case Let
  case SemiColon
  case Function
  case Identifier
  case Return
  case Multiply
  case Divide
  case Comma
  case Exponentiation
  case LeftBrace
  case RightBrace
  case LeftParenthesis
  case RightParenthesis
  case EOF
