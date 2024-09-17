package me.gabriel.seren.compiler

import me.gabriel.seren.analyzer.TypeEnvironment
import me.gabriel.seren.analyzer.inference.LazySymbolBlock.toLazySymbolBlock
import me.gabriel.seren.analyzer.inference.{DefaultTypeInference, LazySymbolBlock}
import me.gabriel.seren.frontend.lexer.{DefaultLexer, Lexer}
import me.gabriel.seren.frontend.parser.{DefaultParser, Parser}
import me.gabriel.seren.frontend.struct.TokenStream

object CompilerApp extends App {
  private val lexer: Lexer = new DefaultLexer
  private val result = lexer.lex(getSourceCode())
  result.fold(
    error => {
      println(s"Lexing Error: ${error.message}")
      sys.exit(1)
    },
    tokens => tokens.foreach(println)
  )
  private val stream = TokenStream(result.right.get)

  private val parser: Parser = new DefaultParser
  private val syntaxTree = parser.parse(stream)
  syntaxTree.fold(
    error => {
      println(s"Parsing Error: ${error.message}")
      sys.exit(1)
    },
    tree => println(tree.prettyPrint)
  )

  private val root = syntaxTree.right.get.root
  private val typeEnvironment = new TypeEnvironment("main", root)
  private val typeInference = new DefaultTypeInference
  private val lazyTypeRoot: LazySymbolBlock = typeEnvironment.root
  
  typeInference.traverseBottomUp(lazyTypeRoot, root)
  lazyTypeRoot.prettyPrintLazyTypes()

  private def getSourceCode(): String = {
    val file = new java.io.File("app.sr")
    val source = scala.io.Source.fromFile(file)
    source.mkString
  }
}
