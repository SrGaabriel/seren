package me.gabriel.tianlong
package struct

sealed class Dependency {

}

case object Dependency {
  case class Constant(
                       name: String,
                       value: ValueReference
                     ) extends Dependency, ValueReference {
    override val dragonType: DragonType = value.dragonType
    override def llvm: String = s"@${name}"
  }
}