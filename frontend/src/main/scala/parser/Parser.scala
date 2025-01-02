package me.gabriel.seren.frontend
package parser

import error.ParsingError
import parser.tree.SyntaxTree
import struct.{Token, TokenStream}

trait Parser {
  def parse(stream: TokenStream): Either[ParsingError, SyntaxTree]
}
