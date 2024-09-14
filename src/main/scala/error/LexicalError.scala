package me.gabriel.soma
package error

trait LexicalError {
  def message: String
}

case class UnexpectedCharacterError(character: Char) extends LexicalError {
  def message: String = s"Unexpected character: $character"
}