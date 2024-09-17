package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.frontend.parser.tree.{SyntaxTreeNode, TypedSyntaxTreeNode}

type Substitution = Map[LazyType, LazyType]
type Constraint = (LazyType, LazyType)

trait TypeInference {
  def traverseBottomUp(block: LazySymbolBlock, node: SyntaxTreeNode): Unit

  def processTypedNode(block: LazySymbolBlock, node: TypedSyntaxTreeNode): LazyType
}
