package me.gabriel.soma

import interpreter.BasicInterpreter
import lexer.{DefaultLexer, Lexer}
import parser.{DefaultParser, Parser}
import struct.TokenStream

val text = "1 + 2 * 3"

object ParserApp extends App {
  private val lexer: Lexer = new DefaultLexer
  private val result = lexer.lex(text)
  result.fold(
    error => println(s"Error: ${error.message}"),
    tokens => tokens.foreach(println)
  )
  private val stream = TokenStream(result.getOrElse(List.empty))

  private val parser: Parser = new DefaultParser
  private val syntaxTree = parser.parse(stream)
  syntaxTree.fold(
    error => println(s"Error: ${error.message}"),
    tree => println(tree.prettyPrint)
  )

  println("Interpreting...")
  private val interpreter = new BasicInterpreter
  interpreter.execute(syntaxTree.getOrElse(null))
}
