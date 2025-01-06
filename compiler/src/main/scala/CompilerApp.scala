package me.gabriel.seren.compiler

import formatter.printError
import io.{CompilerCommandConfig, CompilerCommandLine, CompilerIoHandler}

import me.gabriel.seren.analyzer.TypeEnvironment
import me.gabriel.seren.analyzer.external.{Directive, ModuleManager}
import me.gabriel.seren.analyzer.impl.DefaultSemanticAnalysisManager
import me.gabriel.seren.analyzer.inference.LazySymbolBlock.toLazySymbolBlock
import me.gabriel.seren.analyzer.inference.{HardTypeInference, LazySymbolBlock, TypeSynthesizer}
import me.gabriel.seren.frontend.lexer.{DefaultLexer, Lexer}
import me.gabriel.seren.frontend.parser.{DefaultParser, Parser}
import me.gabriel.seren.frontend.struct.TokenStream
import me.gabriel.seren.llvm.SerenDragonCompiler
import me.gabriel.seren.llvm.target.CompilationTarget
import me.gabriel.seren.logging.{LogLevel, SerenLogger, createLogger, setupTerminalLogging}

@main def main(args: String*): Unit = {
  setupTerminalLogging()
  val logger = createLogger("orchestrator")
  logger.log(LogLevel.INFO, "Starting compiler")

  val cli = new CompilerCommandLine(args.toList)
  val options = cli.parse()
  val io = new CompilerIoHandler
  val lexer: Lexer = new DefaultLexer
  val parser: Parser = new DefaultParser

  val sourceCode = io.readFile(options.inputFile)
  val llFile = compile(logger, options, io, options.inputFile, sourceCode, lexer, parser)
  val llFileName = llFile.getName
  val executableFileName = llFileName.replace(".ll", ".exe")

  val stdlibSourceCode = io.readFile("stdlib/src/lib.sr")
  val stdlibLlFile = compile(logger, options, io, "lib.sr", stdlibSourceCode, lexer, parser)

  val compilationStatusCode = if !options.llvmOnly then
    val compilationCode = io.linkLlFileToExecutable(executableFileName, llFileName, stdlibLlFile.getName)
    if (!options.keepAll) llFile.delete()
    stdlibLlFile.delete()
    Some(compilationCode)
  else None
  logger.dispatchAllQueuedLogs()

  compilationStatusCode match
    case Some(0) =>
      logger.log(LogLevel.INFO, "Compilation finished successfully")
      if options.run then
        logger.log(LogLevel.INFO, s"Running program $executableFileName as per request (--run flag)")
        val exitCode = io.runExecutable(executableFileName)
        val level = if exitCode == 0 then LogLevel.INFO else LogLevel.ERROR
        logger.log(level, s"Program $executableFileName exited with code $exitCode")
    case Some(code) => logger.log(LogLevel.ERROR, s"Compilation failed with code $code")
    case _ => logger.log(LogLevel.INFO, "Compilation finished (LLVM only)")
}

def compile(
  logger: SerenLogger,
  options: CompilerCommandConfig,
  io: CompilerIoHandler,
  fileName: String,
  sourceCode: String,
  lexer: Lexer,
  parser: Parser,
): java.io.File =
  val result = lexer.lex(sourceCode)

  result.left.foreach(error => {
    logger.log(LogLevel.ERROR, s"Lexing error: ${error.message}")
    sys.exit(1)
  })
  val stream = TokenStream(result.toOption.get)

  val syntaxTree = parser.parse(stream)
  syntaxTree.left.foreach(error => {
    printError(
      logger,
      fileName,
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

  val tree = syntaxTree.toOption.get
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
  val target = CompilationTarget(pointerSize = 64)
  val llvmCode = compiler.compile(tree, typeEnvironment, target)
  val llFileName = options.output.getOrElse(fileName.replace(".sr", ".ll"))
  io.writeFile(llFileName, llvmCode)