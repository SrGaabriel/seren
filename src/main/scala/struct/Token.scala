package me.gabriel.soma
package struct

case class Token(value: String, kind: TokenKind)

enum TokenKind:
  case Number(value: Int)
  case Plus
  case Minus
  case Multiply
  case Divide
  case LeftParenthesis
  case RightParenthesis
  case EOF
