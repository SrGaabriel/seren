package me.gabriel.seren.frontend
package parser

enum Type:
  case Any
  case Byte
  case Short
  case Int
  case Long
  case Float
  case Double
  case Boolean
  case Char
  case String
  case Void
  case Struct(name: String)
  case UnknownThis
  case CType(name: String)
  case Vararg(base: Type)
  case Unknown