package me.gabriel.seren.frontend
package error

sealed trait LexicalError {
  val position: Int

  def message: String
}

object LexicalError {
  case class UnexpectedCharacterError(character: Char, position: Int) extends LexicalError {
    def message: String = s"Unexpected character: $character"
  }

  case class UnterminatedString(position: Int) extends LexicalError {
    def message: String = "Unterminated string literal"
  }
}