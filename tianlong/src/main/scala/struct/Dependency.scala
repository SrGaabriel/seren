package me.gabriel.tianlong
package struct

sealed class Dependency {

}

object Dependency {
  case class Constant(
                       name: String,
                       value: ValueReference
                     ) extends Dependency
}