package me.gabriel.seren.analyzer
package inference

sealed trait TypeConstraint {
  def left: LazyType

  def right: LazyType
}

case class EqualityConstraint(left: LazyType, right: LazyType) extends TypeConstraint

case class StructFieldConstraint(struct: LazyType, field: String, fieldType: LazyType) extends TypeConstraint {
  override def left: LazyType = struct

  override def right: LazyType = fieldType
}

case class Substitution(mappings: Map[String, LazyType]) {
  def compose(other: Substitution): Substitution = {
    Substitution(
      mappings ++ other.mappings.map { case (v, t) => v -> apply(t) }
    )
  }

  def apply(t: LazyType): LazyType = t match {
    case TypeVariable(name) => mappings.getOrElse(name, t)
    case TypeFunction(params, ret) =>
      TypeFunction(params.map(apply), apply(ret))
    case TypeAccess(struct, field) =>
      TypeAccess(apply(struct), field)
    case TypeCall(name, args) =>
      TypeCall(name, args.map(apply))
    case lit: TypeLiteral => lit
  }
}

object Substitution {
  def empty: Substitution = Substitution(Map.empty)
}