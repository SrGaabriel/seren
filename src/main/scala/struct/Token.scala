package me.gabriel.soma
package struct

case class Token(value: String, kind: TokenKind)

enum TokenKind:
  case BOF
  case Number
  case Plus
  case Minus
  case Multiply
  case Divide
  case LeftParenthesis
  case RightParenthesis
  case EOF
