package me.gabriel.seren.analyzer
package impl.node

import error.AnalysisResult
import impl.SemanticAnalyzer

import me.gabriel.seren.frontend.parser.tree.FunctionDeclarationNode

implicit val functionAnalyzer: SemanticAnalyzer[FunctionDeclarationNode] =
  (block: SymbolBlock, node: FunctionDeclarationNode, result: AnalysisResult) => {
    val functionBlock = block.surfaceSearchChild(node)
    functionBlock.get
  }
