package me.gabriel.seren.analyzer
package impl

import error.AnalysisResult
import external.ModuleManager

trait SemanticAnalyzer[T] {
  def analyze(
    module: ModuleManager,
    block: SymbolBlock,
    node: T,
    currentResult: AnalysisResult
  ): SymbolBlock
}
