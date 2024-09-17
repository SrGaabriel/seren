package me.gabriel.seren.analyzer
package inference

object LazyTypeUnifier {
  def union(left: LazyType, right: LazyType): Option[Substitution] = {
    (left, right) match {
      case (TypeVariable(a), TypeVariable(b))
        if a == b => Some(Map.empty)

      case (tv @ TypeVariable(a), t) => Some(Map(tv -> t))
      case (t, tv @ TypeVariable(a)) => Some(Map(tv -> t))
      case (TypeLiteral(a), TypeLiteral(b)) if a == b => Some(Map.empty)
      case (TypeFunction(leftParams, leftReturnType), TypeFunction(rightParams, rightReturnType)) =>
        val paramsSubstitution = leftParams.zip(rightParams).foldLeft[Option[Substitution]](Some(Map.empty)) {
          case (Some(substitution), (leftParam, rightParam)) =>
            union(applySubstitution(substitution, leftParam), applySubstitution(substitution, rightParam))
              .map(composeSubstitutions(substitution, _))
          case _ => None
        }

        paramsSubstitution.flatMap { substitution =>
          union(applySubstitution(substitution, leftReturnType), applySubstitution(substitution, rightReturnType))
            .map(composeSubstitutions(substitution, _))
        }
      case _ => None
    }
  }

  def applySubstitution(substitution: Substitution, lazyType: LazyType): LazyType = {
    lazyType match {
      case tv @ TypeVariable(_) => substitution.getOrElse(tv, tv)
      case TypeLiteral(_) => lazyType
      case TypeFunction(parameters, to) =>
        TypeFunction(parameters.map(applySubstitution(substitution, _)), applySubstitution(substitution, to))
    }
  }

  def composeSubstitutions(left: Substitution, right: Substitution): Substitution =
    right.map { case (k, v) => k -> applySubstitution(left, v) } ++ left
}
