package me.gabriel.seren.llvm

import session.TianlongCompilerSession
import target.CompilationTarget

import me.gabriel.seren.analyzer.TypeEnvironment
import me.gabriel.seren.frontend.parser.tree.SyntaxTree
import me.gabriel.tianlong.transcriber.{DragonIrTranscriber, TianlongIrTranscriber}

class SerenDragonCompiler {
  val transcriber: DragonIrTranscriber = TianlongIrTranscriber()

  def compile(
    tree: SyntaxTree,
    typeEnvironment: TypeEnvironment,
    target: CompilationTarget
  ): String = {
    val session = new TianlongCompilerSession(tree, typeEnvironment, target)
    session.generateTree()
    val module = session.finish()
    transcriber.transcribe(module)
  }
}
