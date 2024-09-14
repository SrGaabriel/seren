package me.gabriel.seren.frontend
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

case class RootNode(token: Token, children: List[SyntaxTreeNode]) extends SyntaxTreeNode {
  override def toString: String = s"RootNode(children=${children.size})"
}

case class NumericNode(token: Token) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"NumericNode(${token.value})"
}

case class BinaryOperationNode(
                                token: Token,
                                op: BinaryOp,
                                left: SyntaxTreeNode,
                                right: SyntaxTreeNode
                              ) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List(left, right)

  override def toString: String = s"BinaryOpNode($left, $op, $right)"
}

case class StringLiteralNode(token: Token) extends SyntaxTreeNode {
  val value: String = token.value

  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"StringLiteralNode($value)"
}

case class FunctionDeclarationNode(
                                  token: Token,
                                  name: String,
                                  parameters: List[FunctionParameterNode],
                                  children: List[SyntaxTreeNode]
                                  ) extends SyntaxTreeNode {
  override def toString: String = s"FunctionDeclarationNode($name, $parameters, $children)"
}

case class FunctionParameterNode(token: Token, name: String) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"FunctionParameterNode($name)"
}

case class FunctionCallNode(
                            token: Token,
                            name: String,
                            arguments: List[SyntaxTreeNode]
                            ) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = arguments

  override def toString: String = s"FunctionCallNode($name, $arguments)"
}