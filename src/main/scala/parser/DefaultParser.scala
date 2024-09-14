package me.gabriel.soma
package parser

import error.{NotImplementedFeatureError, ParsingError}
import struct.Token

import tree.SyntaxTree

class DefaultParser extends Parser {
    def parse(tokens: List[Token]): Either[ParsingError, SyntaxTree] = {
        Left(NotImplementedFeatureError("parsing"))
    }
}