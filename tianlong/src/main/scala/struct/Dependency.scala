package me.gabriel.tianlong
package struct

sealed class Dependency {
}

case object Dependency {
  case class Constant(
                       name: String,
                       value: ValueReference,
                       linkage: Option[LinkageType] = None
                     ) extends Dependency, ValueReference {
    override val dragonType: DragonType = value.dragonType
    override def llvm: String = s"@$name"
  }

  case class Function(
                       name: String,
                       returnType: DragonType,
                       parameters: List[DragonType]
                     ) extends Dependency {
  }
  
  case class Struct(
                      name: String,
                      fields: List[DragonType]
                   ) extends Dependency {
    
  }
}