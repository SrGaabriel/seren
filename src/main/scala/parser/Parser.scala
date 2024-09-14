package me.gabriel.soma
package parser

import error.ParsingError
import struct.Token
import tree.SyntaxTree

trait Parser {
  def parse(tokens: List[Token]): Either[ParsingError, SyntaxTree]
}
