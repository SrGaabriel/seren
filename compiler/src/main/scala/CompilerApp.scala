package me.gabriel.seren.compiler

import formatter.printError
import io.{CompilerCommandLine, CompilerIoHandler}

import me.gabriel.seren.analyzer.TypeEnvironment
import me.gabriel.seren.analyzer.external.{Directive, ModuleManager}
import me.gabriel.seren.analyzer.impl.DefaultSemanticAnalysisManager
import me.gabriel.seren.analyzer.inference.LazySymbolBlock.toLazySymbolBlock
import me.gabriel.seren.analyzer.inference.{HardTypeInference, LazySymbolBlock, TypeSynthesizer}
import me.gabriel.seren.frontend.lexer.{DefaultLexer, Lexer}
import me.gabriel.seren.frontend.parser.{DefaultParser, Parser}
import me.gabriel.seren.frontend.struct.TokenStream
import me.gabriel.seren.llvm.SerenDragonCompiler
import me.gabriel.seren.logging.{LogLevel, createLogger, setupTerminalLogging}

@main def main(args: String*): Unit = {
  setupTerminalLogging()
  val logger = createLogger("orchestrator")
  logger.log(LogLevel.INFO, "Starting compiler")

  val cli = new CompilerCommandLine(args.toList)
  val options = cli.parse()
  val io = new CompilerIoHandler
  val lexer: Lexer = new DefaultLexer
  val sourceCode = io.readFile(options.inputFile)
  val result = lexer.lex(sourceCode)

  result.left.foreach(error => {
    logger.log(LogLevel.ERROR, s"Lexing error: ${error.message}")
    sys.exit(1)
  })
  val stream = TokenStream(result.right.get)

  val parser: Parser = new DefaultParser
  val syntaxTree = parser.parse(stream)
  syntaxTree.left.foreach(error => {
    printError(
      logger,
      options.inputFile,
      sourceCode,
      "PARSING",
      error.token.position,
      error.token.position,
      error.message
    )
    logger.log(LogLevel.ERROR, error)
    sys.exit(1)
  })

  val moduleManager = ModuleManager(
    directive = Directive(module = "root", subdirectories = List.empty)
  )

  val tree = syntaxTree.right.get
  val root = tree.root
  val typeEnvironment = new TypeEnvironment("main", root)
  val typeInference = new HardTypeInference
  val lazyTypeRoot: LazySymbolBlock = typeEnvironment.root

  typeInference.traverseBottomUp(moduleManager, lazyTypeRoot, root)
  TypeSynthesizer.updateTreeTypes(moduleManager, lazyTypeRoot)

  val analysisManager = DefaultSemanticAnalysisManager(moduleManager)
  val analysisResult = analysisManager.analyzeTree(typeEnvironment, tree)
  if (analysisResult.errors.nonEmpty) {
    val errors = analysisResult.errors
      .map(err => s"  |${err.getClass.getSimpleName}: ${err.message}")
      .mkString("\n")
    logger.log(LogLevel.ERROR, s"Semantic analysis failed with ${analysisResult.errors.size} errors\n$errors")
    sys.exit(1)
  }

  val compiler = SerenDragonCompiler()
  val llvmCode = compiler.compile(tree, typeEnvironment)

  val llFileName = options.output.getOrElse(options.inputFile.replace(".sr", ".ll"))
  val llFile = io.writeFile(llFileName, llvmCode)
  if (!options.llvmOnly) {
    io.linkLlFileToExecutable(llFileName, llFileName.replace(".ll", ".exe"))
    if (!options.keepAll) llFile.delete()
  }
  logger.lazyLog(LogLevel.INFO, "Compilation finished")
  logger.dispatchAllQueuedLogs()
}