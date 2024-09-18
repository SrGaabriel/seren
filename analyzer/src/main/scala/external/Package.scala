package me.gabriel.seren.analyzer
package external

import me.gabriel.seren.frontend.parser.Type

sealed trait Package {
  val name: String
  val directive: Directive
}

object Package {
  case class Function(
                name: String,
                directive: Directive,
                returnType: Type,
                parameters: List[Type]
              ) extends Package
}