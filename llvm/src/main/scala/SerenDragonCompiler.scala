package me.gabriel.seren.llvm

import session.TianlongCompilerSession

import me.gabriel.seren.analyzer.TypeEnvironment
import me.gabriel.seren.frontend.parser.tree.SyntaxTree
import me.gabriel.tianlong.transcriber.{DragonIrTranscriber, TianlongIrTranscriber}

class SerenDragonCompiler {
  val transcriber: DragonIrTranscriber = TianlongIrTranscriber()
  
  def compile(
               tree: SyntaxTree,
               typeEnvironment: TypeEnvironment
             ): String = {
    val session = new TianlongCompilerSession(tree, typeEnvironment)
    val module = session.finish()
    transcriber.transcribe(module)
  }
}
