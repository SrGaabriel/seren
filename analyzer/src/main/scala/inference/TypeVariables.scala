package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.frontend.parser.Type

sealed trait LazyType

case class TypeVariable(name: String) extends LazyType
case class TypeCall(name: String, params: List[LazyType]) extends LazyType
case class TypeLiteral(actualType: Type) extends LazyType
case class TypeAccess(structType: LazyType, field: String) extends LazyType
case class TypeFunction(from: List[LazyType], to: LazyType) extends LazyType