package me.gabriel.seren

import interpreter.BasicInterpreter
import lexer.{DefaultLexer, Lexer}
import parser.{DefaultParser, Parser}
import struct.TokenStream

val text = """1 + 2 * 3 + 2^3 * 4^5 + "Hello" + 1"""

object ParserApp extends App {
  private val lexer: Lexer = new DefaultLexer
  private val result = lexer.lex(text)
  result.fold(
    error => {
      println(s"Error: ${error.message}")
      sys.exit(1)
    },
    tokens => tokens.foreach(println)
  )
  private val stream = TokenStream(result.right.get)

  private val parser: Parser = new DefaultParser
  private val syntaxTree = parser.parse(stream)
  syntaxTree.fold(
    error => {
      println(s"Error: ${error.message}")
      sys.exit(1)
    },
    tree => println(tree.prettyPrint)
  )

  println("Interpreting...")
  private val interpreter = new BasicInterpreter
  interpreter.execute(syntaxTree.right.get)
}
