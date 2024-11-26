package me.gabriel.seren.frontend
package parser.tree

import parser.Type
import struct.{BinaryOp, FunctionModifier, Token}

case class SyntaxTree(root: RootNode) {
  def prettyPrint: String = {
    def prettyPrintNode(node: SyntaxTreeNode, indent: Int): String = {
      val indentation = "  " * indent
      val children = node.children.map(prettyPrintNode(_, indent + 1)).mkString("\n")
      s"$indentation$node\n$children"
    }

    prettyPrintNode(root, 0)
  }
  
  def prettyPrintTyped: String = {
    def prettyPrintNode(node: SyntaxTreeNode, indent: Int): String = {
      val indentation = "  " * indent
      val children = node.children.map(prettyPrintNode(_, indent + 1)).mkString("\n")
      node match {
        case typedNode: TypedSyntaxTreeNode =>
          s"$indentation$typedNode (type=${typedNode.nodeType})\n$children"
        case _ =>
          s"$indentation$node\n$children"
      }
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

case class BlockNode(token: Token, children: List[SyntaxTreeNode]) extends SyntaxTreeNode {
  override def toString: String = s"BlockNode(children=${children.size})"
}

case class NumericNode(token: Token, var nodeType: Type) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"NumericNode(${token.value})"
}

class BinaryOperationNode(
                                val token: Token,
                                val op: BinaryOp,
                                val left: TypedSyntaxTreeNode,
                                val right: TypedSyntaxTreeNode,
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

class FunctionDeclarationNode(
                                    val token: Token,
                                    val name: String,
                                    var nodeType: Type.Function,
                                    val parameters: List[FunctionParameterNode],
                                    val modifiers: Set[FunctionModifier],
                                    val block: BlockNode
                                  ) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = parameters :+ block
  
  def nodeType_$eq(nodeType: Type): Unit = {
    this.nodeType = nodeType.asInstanceOf[Type.Function]
  }

  override def toString: String = s"FunctionDeclarationNode($name, $parameters, ${nodeType.returnType})"
}

case class FunctionParameterNode(token: Token, name: String, var nodeType: Type) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"FunctionParameterNode($name)"
}

class FunctionCallNode(
                            val token: Token,
                            val name: String,
                            var arguments: List[TypedSyntaxTreeNode]
                            ) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = arguments
  var nodeType: Type = Type.Unknown

  override def toString: String = s"FunctionCallNode($name, $arguments)"
}

case class ReturnNode(token: Token, value: TypedSyntaxTreeNode) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List(value)

  override def toString: String = s"ReturnNode($value)"
}

class AssignmentNode(val token: Token, val name: String, val value: TypedSyntaxTreeNode) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List(value)
  var nodeType: Type = value.nodeType

  override def toString: String = s"AssignmentNode($name, $value)"
}

class ReferenceNode(val token: Token, val name: String, var nodeType: Type) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"ReferenceNode($name, $nodeType)"
}

class StructDeclarationNode(
                              val token: Token,
                              val name: String,
                              val fields: List[StructFieldNode],
                              val functions: List[FunctionDeclarationNode]
                           ) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = fields ++ functions
  
  override def toString: String = s"StructDeclarationNode($name, $fields)"
}

class StructFieldNode(
                       val token: Token,
                       val name: String,
                       var nodeType: Type
                     ) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty

  override def toString: String = s"StructFieldNode($name)"
}

class StructFieldAccessNode(
                            val token: Token,
                            val struct: TypedSyntaxTreeNode,
                            val fieldName: String,
                            var nodeType: Type
                           ) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List(struct)

  override def toString: String = s"StructFieldAccessNode($token, $struct, $fieldName)"
}

class StructInstantiationNode(
                                val token: Token,
                                val structName: String,
                                var nodeType: Type,
                                val arguments: List[TypedSyntaxTreeNode],
                             ) extends TypedSyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = arguments

  override def toString: String = s"StructInstantiationNode($structName, $arguments)"
}

class EnumDeclarationNode(
                           val token: Token,
                           val name: String,
                           val variants: List[EnumVariantNode]
                         ) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = variants

  override def toString: String = s"EnumDeclarationNode($name, $variants)"
}

class EnumVariantNode(
                       val token: Token,
                       val name: String,
                       val types: List[Type]
                     ) extends SyntaxTreeNode {
  override val children: List[SyntaxTreeNode] = List.empty
  
  override def toString: String = s"EnumVariantNode($name, $types)"
}