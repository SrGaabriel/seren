package me.gabriel.soma
package parser.tree

import struct.Token
import struct.BinaryOp

case class SyntaxTree(root: RootNode) {
  def prettyPrint: String = {
    def prettyPrintNode(node: SyntaxTreeNode, indent: Int): String = {
      val indentation = "  " * indent
      val children = node.children.map(prettyPrintNode(_, indent + 1)).mkString("\n")
      s"$indentation$node\n$children"
    }

    prettyPrintNode(root, 0)
  }
}

sealed trait SyntaxTreeNode {
  val token: Token
  val children: List[SyntaxTreeNode]
}

case class RootNode(token: Token, children: List[SyntaxTreeNode]) extends SyntaxTreeNode

case class NumericNode(token: Token, children: List[SyntaxTreeNode]) extends SyntaxTreeNode

case class BinaryOperationNode(
                                token: Token,
                                op: BinaryOp,
                                left: SyntaxTreeNode,
                                right: SyntaxTreeNode
                              ) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List(left, right)
}
