package me.gabriel.seren.analyzer
package impl

import error.AnalysisResult

trait SemanticAnalyzer[T] {
  def analyze(
             block: SymbolBlock,
             node: T,
             currentResult: AnalysisResult
             ): SymbolBlock
}
