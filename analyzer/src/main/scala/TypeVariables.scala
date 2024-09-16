sealed trait TypeExpression

case class TypeVariable(name: String) extends TypeExpression
case class TypeLiteral(name: String) extends TypeExpression
case class TypeFunction(from: TypeExpression, to: TypeExpression) extends TypeExpression