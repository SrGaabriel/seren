package me.gabriel.seren.analyzer

import error.AnalysisResult

import me.gabriel.seren.frontend.parser.tree.SyntaxTree

trait SemanticAnalysisManager {
  def analyzeTree(typeEnvironment: TypeEnvironment, tree: SyntaxTree): AnalysisResult
}
