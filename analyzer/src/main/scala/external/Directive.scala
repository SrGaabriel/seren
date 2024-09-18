package me.gabriel.seren.analyzer
package external

case class Directive(
               val module: String,
               val subdirectories: List[String]
               )
