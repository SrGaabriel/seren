package me.gabriel.seren
package struct

case class Token(value: String, kind: TokenKind)

enum TokenKind:
  case BOF
  case Number
  case Plus
  case Minus
  case String
  case Multiply
  case Divide
  case Exponentiation
  case LeftParenthesis
  case RightParenthesis
  case EOF
