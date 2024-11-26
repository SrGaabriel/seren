package me.gabriel.seren.analyzer
package inference

import external.ModuleManager

import me.gabriel.seren.frontend.parser.tree.{SyntaxTreeNode, TypedSyntaxTreeNode}

trait TypeInference {
  def traverseBottomUp(
                        module: ModuleManager,
                        block: LazySymbolBlock,
                        node: SyntaxTreeNode
                      ): Unit

  def processTypedNode(
                        block: LazySymbolBlock,
                        node: TypedSyntaxTreeNode
                      ): LazyType
}
