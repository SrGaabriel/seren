package me.gabriel.tianlong
package struct

sealed class Dependency {
  case class Constant(
                       name: String,
                       dragonType: DragonType,
                       value: ValueReference
                     )
}
