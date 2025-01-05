package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.frontend.parser.Type

enum LazyType:
  case TypeVariable(name: String)
  case TypeCall(name: String, params: List[LazyType])
  case TypeLiteral(actualType: Type) extends LazyType
  case TypeAccess(structType: LazyType, field: String)
  case TypeFunction(from: List[LazyType], to: LazyType)