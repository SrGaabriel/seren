package me.gabriel.seren.analyzer
package impl.node

import error.AnalysisResult
import external.ModuleManager
import impl.SemanticAnalyzer

import me.gabriel.seren.frontend.parser.tree.StructDeclarationNode

implicit val structAnalyzer: SemanticAnalyzer[StructDeclarationNode] =
  (module: ModuleManager, block: SymbolBlock, node: StructDeclarationNode, result: AnalysisResult) => {
    val declarationNode = block.surfaceSearchChild(node)
    declarationNode.get
  }
