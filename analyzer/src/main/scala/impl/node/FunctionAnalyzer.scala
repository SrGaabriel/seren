package me.gabriel.seren.analyzer
package impl.node

import error.AnalysisResult
import impl.SemanticAnalyzer

import me.gabriel.seren.analyzer.external.ModuleManager
import me.gabriel.seren.frontend.parser.tree.FunctionDeclarationNode

implicit val functionAnalyzer: SemanticAnalyzer[FunctionDeclarationNode] =
  (module: ModuleManager, block: SymbolBlock, node: FunctionDeclarationNode, result: AnalysisResult) => {
    val functionBlock = block.surfaceSearchChild(node)
    functionBlock.get
  }
