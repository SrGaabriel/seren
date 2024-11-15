package me.gabriel.seren.analyzer
package error

import me.gabriel.seren.frontend.parser.tree.SyntaxTreeNode

sealed trait AnalysisError {
  def message: String
  val node: SyntaxTreeNode
}

object AnalysisError {
  case class FeatureNotImplemented(
                                  feature: String,
                                  node: SyntaxTreeNode
                                  ) extends  AnalysisError {
    def message = s"The feature $feature has not been implemented yet"
  }
  
  case class FunctionNotDefined(
                                name: String,
                                node: SyntaxTreeNode
                                ) extends AnalysisError {
    def message = s"Function $name is not defined"
  }
}
