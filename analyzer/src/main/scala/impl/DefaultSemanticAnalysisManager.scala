package me.gabriel.seren.analyzer
package impl

import error.AnalysisResult
import impl.node.*

import me.gabriel.seren.frontend.parser.tree.*

class DefaultSemanticAnalysisManager extends SemanticAnalysisManager {
  override def analyzeTree(typeEnvironment: TypeEnvironment, tree: SyntaxTree): AnalysisResult = {
    val results = new AnalysisResult()
    magicallyAnalyzeNode(
      block = typeEnvironment.root,
      node = tree.root,
      currentResult = results
    )
    results
  }

  def magicallyAnalyzeNode(block: SymbolBlock, node: SyntaxTreeNode, currentResult: AnalysisResult): Unit = {
    val newBlock = node match {
      case node: AssignmentNode => analyzeNode(block, node, currentResult)
      case node: FunctionDeclarationNode => analyzeNode(block, node, currentResult)
      case _ => block
    }

    node.children.foreach(child => magicallyAnalyzeNode(newBlock, child, currentResult))
  }

  def analyzeNode[T](
                    block: SymbolBlock,
                    node: T,
                    currentResult: AnalysisResult
                    )(implicit analyzer: SemanticAnalyzer[T]): SymbolBlock = {
    analyzer.analyze(block, node, currentResult)
  }
}
