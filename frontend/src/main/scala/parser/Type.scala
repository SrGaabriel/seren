package me.gabriel.seren.frontend
package parser

enum Type:
  case Any
  case Byte
  case Short
  case Int
  case Long
  case Float
  case Usize
  case Double
  case Boolean
  case Char
  case String
  case Void
  case Pointer(base: Type)
  case Struct(name: String, fields: Map[String, Type])
  case CType(name: String)
  case Vararg(base: Type)
  case Function(params: List[Type], returnType: Type)
  case Unknown
  case UnknownThis
  case UnknownIdentifier(name: String)