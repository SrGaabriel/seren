package me.gabriel.seren.compiler

import me.gabriel.seren.analyzer.TypeEnvironment
import me.gabriel.seren.analyzer.external.{Directive, ModuleManager}
import me.gabriel.seren.analyzer.impl.DefaultSemanticAnalysisManager
import me.gabriel.seren.analyzer.inference.LazySymbolBlock.toLazySymbolBlock
import me.gabriel.seren.analyzer.inference.{DefaultTypeInference, LazySymbolBlock, TypeSynthesizer}
import me.gabriel.seren.frontend.lexer.{DefaultLexer, Lexer}
import me.gabriel.seren.frontend.parser.{DefaultParser, Parser, Type}
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
    tree => println(tree.prettyPrintTyped)
  )

  private val moduleManager = ModuleManager(
    directive = Directive(module = "root", subdirectories = List.empty)
  )
  moduleManager.addLocalFunction(
    name = "print_line",
    params = List(Type.Any),
    returnType = Type.Void
  )

  private val tree = syntaxTree.right.get
  private val root = tree.root
  private val typeEnvironment = new TypeEnvironment("main", root)
  private val typeInference = new DefaultTypeInference
  private val lazyTypeRoot: LazySymbolBlock = typeEnvironment.root
  
  typeInference.traverseBottomUp(moduleManager, lazyTypeRoot, root)
  println("===========================")
  println(tree.prettyPrintTyped)

  val analysisManager = DefaultSemanticAnalysisManager()
  val analysisResult = analysisManager.analyzeTree(typeEnvironment, tree)
  if (analysisResult.errors.nonEmpty) {
    println(s"There have been ${analysisResult.errors.size} errors:")
    analysisResult.errors.foreach(error => println(s"  |${error.getClass.getSimpleName}: ${error.message}"))
    sys.exit(1)
  }

  private def getSourceCode(): String = {
    val file = new java.io.File("app.sr")
    val source = scala.io.Source.fromFile(file)
    source.mkString
  }
}
