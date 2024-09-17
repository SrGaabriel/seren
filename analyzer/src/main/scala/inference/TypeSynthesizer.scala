package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.frontend.parser.tree.{SyntaxTreeNode, TypedSyntaxTreeNode}

object TypeSynthesizer {
  def union(left: LazyType, right: LazyType): Option[Substitution] = {
    (left, right) match {
      case (TypeVariable(a), TypeVariable(b))
        if a == b => Some(Map.empty)

      case (tv @ TypeVariable(a), t) => Some(Map(tv -> t))
      case (t, tv @ TypeVariable(a)) => Some(Map(tv -> t))
      case (TypeLiteral(a), TypeLiteral(b)) if a == b => Some(Map.empty)
      case (TypeFunction(leftParams, leftReturnType), TypeFunction(rightParams, rightReturnType)) =>
        for {
          leftUnion <- leftParams.zip(rightParams).foldLeft(Option(Map.empty: Substitution)) {
            case (Some(substitution), (left, right)) =>
              union(applySubstitution(substitution, left), applySubstitution(substitution, right))
                .map(substitution ++ _)
            case _ => None
          }
          rightUnion <- union(applySubstitution(leftUnion, leftReturnType), applySubstitution(leftUnion, rightReturnType))
        } yield leftUnion ++ rightUnion
      case _ => None
    }
  }

  def applySubstitution(substition: Substitution, lazyType: LazyType): LazyType = {
    lazyType match {
      case tv @ TypeVariable(_) => substition.getOrElse(tv, tv)
      case TypeLiteral(_) => lazyType
      case TypeFunction(parameters, to) =>
        TypeFunction(parameters.map(applySubstitution(substition, _)), applySubstitution(substition, to))
    }
  }

  def composeSubstitutions(left: Substitution, right: Substitution): Substitution =
    right.map { case (k, v) => k -> applySubstitution(left, v) } ++ left
}
