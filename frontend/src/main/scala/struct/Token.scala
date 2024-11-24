package me.gabriel.seren.frontend
package struct

import parser.Type

case class Token(value: String, kind: TokenKind)

enum TokenKind:
  case BOF
  case NumberLiteral(suffixType: Option[Type])
  case Plus
  case Minus
  case NewLine
  case StringLiteral
  case Let
  case SemiColon
  case Function
  case Struct
  case This
  case Identifier
  case Return
  case Multiply
  case Vararg
  case Divide
  case Comma
  case Modulo
  case Dot
  case LeftBrace
  case Assign
  case External
  case TypeDeclaration
  case AnyType
  case VoidType
  case StringType
  case Int8Type
  case Int16Type
  case Int32Type
  case Int64Type
  case UInt8Type
  case UInt16Type
  case UInt32Type
  case UInt64Type
  case Float32Type
  case Float64Type
  case RightBrace
  case LeftParenthesis
  case RightParenthesis
  case LeftAngleBracket
  case RightAngleBracket
  case EOF
