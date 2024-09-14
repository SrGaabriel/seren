package me.gabriel.soma
package parser

trait Parser {
  def parse(input: String): Either[String, Int]
}
