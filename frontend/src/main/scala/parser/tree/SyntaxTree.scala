package me.gabriel.seren.frontend
package parser.tree

import struct.Token
import struct.BinaryOp
import me.gabriel.seren.frontend.parser.Type

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

sealed trait TypedSyntaxTreeNode extends SyntaxTreeNode {
  var nodeType: Type
}

case class RootNode(token: Token, children: List[SyntaxTreeNode]) extends SyntaxTreeNode {
  override def toString: String = s"RootNode(children=${children.size})"
}

case class NumericNode(token: Token, var nodeType: Type) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"NumericNode(${token.value})"
}

case class BinaryOperationNode(
                                token: Token,
                                op: BinaryOp,
                                left: TypedSyntaxTreeNode,
                                right: TypedSyntaxTreeNode,
                              ) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List(left, right)
  var nodeType: Type = left.nodeType

  override def toString: String = s"BinaryOpNode($left, $op, $right)"
}

case class StringLiteralNode(token: Token) extends TypedSyntaxTreeNode {
  val value: String = token.value
  var nodeType: Type = Type.String

  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"StringLiteralNode($value)"
}

case class FunctionDeclarationNode(
                                  token: Token,
                                  name: String,
                                  returnType: Type,
                                  parameters: List[FunctionParameterNode],
                                  children: List[SyntaxTreeNode]
                                  ) extends TypedSyntaxTreeNode {
  var nodeType: Type = returnType
  
  override def toString: String = s"FunctionDeclarationNode($name, $parameters, $returnType)"
}

case class FunctionParameterNode(token: Token, name: String) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"FunctionParameterNode($name)"
}

case class FunctionCallNode(
                            token: Token,
                            name: String,
                            arguments: List[SyntaxTreeNode]
                            ) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = arguments
  var nodeType: Type = Type.Unknown

  override def toString: String = s"FunctionCallNode($name, $arguments)"
}

case class ReturnNode(token: Token, value: TypedSyntaxTreeNode) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List(value)
  var nodeType: Type = value.nodeType

  override def toString: String = s"ReturnNode($value)"
}