package me.gabriel.seren.frontend
package parser

import error.ParsingError
import struct.Token
import struct.TokenStream
import tree.SyntaxTree

trait Parser {
  def parse(stream: TokenStream): Either[ParsingError, SyntaxTree]
}
