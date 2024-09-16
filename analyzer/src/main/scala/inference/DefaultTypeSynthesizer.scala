package me.gabriel.seren.analyzer
package inference

class DefaultTypeSynthesizer extends TypeSynthesizer {
  override def union(left: LazyType, right: LazyType): Option[LazyType] = {
    (left, right) match {
        case (TypeVariable(a), TypeVariable(b)) if a == b => Some(TypeVariable(a))
      case (TypeVariable(a), _) => Some(TypeVariable(a))
      case (_, TypeVariable(b)) => Some(TypeVariable(b))
      case (TypeLiteral(a), TypeLiteral(b)) if a == b => Some(TypeLiteral(a))
      case (TypeFunction(a1, a2), TypeFunction(b1, b2)) =>
        for {
          leftUnion <- union(a1, b1)
          rightUnion <- union(a2, b2)
        } yield TypeFunction(leftUnion, rightUnion)
      case _ => None
    }
  }

  override def substitute(oldType: LazyType, newType: LazyType): Unit = {

  }
}
