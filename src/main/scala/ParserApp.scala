package me.gabriel.soma

import lexer.Lexer
import lexer.DefaultLexer
import parser.Parser
import parser.DefaultParser

import me.gabriel.soma.struct.TokenStream

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
}
