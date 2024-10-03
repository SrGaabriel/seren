package me.gabriel.tianlong
package struct

sealed class Dependency {

}

case object Dependency {
  case class Constant(
                       name: String,
                       value: ValueReference
                     ) extends Dependency
}