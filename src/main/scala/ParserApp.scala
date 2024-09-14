package me.gabriel.soma

import lexer.DefaultLexer

val text = s"1 + 2 * 3"

object ParserApp extends App {
  private val lexer = new DefaultLexer
  private val result = lexer.lex(text)
  result.fold(
    error => println(s"Error: ${error.message}"),
    tokens => tokens.foreach(println)
  )
}
