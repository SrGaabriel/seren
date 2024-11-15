package me.gabriel.seren.analyzer
package impl.node

import error.AnalysisResult
import external.ModuleManager
import impl.SemanticAnalyzer

import me.gabriel.seren.frontend.parser.tree.AssignmentNode

implicit val assignmentAnalyzer: SemanticAnalyzer[AssignmentNode] =
  (module: ModuleManager, block: SymbolBlock, node: AssignmentNode, result: AnalysisResult) => {
    block
  }
