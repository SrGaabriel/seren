package me.gabriel.soma
package lexer

import error.LexicalError
import struct.Token

trait Lexer {
  def lex(input: String): Either[LexicalError, List[Token]]
}
