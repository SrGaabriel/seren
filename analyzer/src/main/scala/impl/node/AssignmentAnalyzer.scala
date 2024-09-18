package me.gabriel.seren.analyzer
package impl.node

import error.AnalysisResult
import impl.SemanticAnalyzer

import me.gabriel.seren.frontend.parser.tree.AssignmentNode

implicit val assignmentAnalyzer: SemanticAnalyzer[AssignmentNode] =
  (block: SymbolBlock, node: AssignmentNode, result: AnalysisResult) => {
    block
  }
