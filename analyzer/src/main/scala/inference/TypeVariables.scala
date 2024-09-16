package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.frontend.parser.Type

sealed trait LazyType

case class TypeVariable(name: String) extends LazyType
case class TypeLiteral(actualType: Type) extends LazyType
case class TypeFunction(from: LazyType, to: LazyType) extends LazyType

sealed trait TypedExpr

case class VarRefExpr(name: String) extends TypedExpr
case class LetExpr(name: String, value: TypedExpr) extends TypedExpr
case class LambdaExpr(name: String, body: TypedExpr) extends TypedExpr
case class CallExpr(name: String, args: List[TypedExpr]) extends TypedExpr

