package me.gabriel.seren.analyzer
package impl.node

import error.AnalysisResult
import external.ModuleManager
import impl.SemanticAnalyzer

import me.gabriel.seren.analyzer.error.AnalysisError.FunctionNotDefined
import me.gabriel.seren.frontend.parser.tree.FunctionCallNode

implicit val callAnalyzer: SemanticAnalyzer[FunctionCallNode] =
  (module: ModuleManager, block: SymbolBlock, node: FunctionCallNode, result: AnalysisResult) => {
    val function = module.searchFunction(node.name)
    if (function.isEmpty) {
      result.error(FunctionNotDefined(node.name, node))
    }
    
    block
  }