package me.gabriel.seren.analyzer
package inference

import scala.collection.mutable

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.{FunctionDeclarationNode, ReturnNode, SyntaxTreeNode, TypedSyntaxTreeNode}

class DefaultTypeInference extends TypeInference {
  private var typeVarCounter = 0
  private def newTypeVar(): LazyType = TypeVariable(s"t${typeVarCounter += 1; typeVarCounter}")

  override def traverseBottomUp(block: LazySymbolBlock, node: SyntaxTreeNode): Unit = {
    val actualBlock = node match {
      case function: FunctionDeclarationNode => block.createChild(function)
      case _ => block
    }

    node.children.foreach(traverseBottomUp(actualBlock, _))

    node match {
      case typedNode: TypedSyntaxTreeNode => processTypedNode(actualBlock, typedNode)
      case _ =>
    }
  }

  override def processTypedNode(block: LazySymbolBlock, node: TypedSyntaxTreeNode): Unit = {
    node match {
      case functionNode: FunctionDeclarationNode =>
        val paramTypes = functionNode.parameters.map(_ => newTypeVar())

        val typeFun = TypeFunction(
          from = paramTypes,
          to = TypeLiteral(node.nodeType)
        )
        println(s"Function: (${block.id}, $typeFun)")
        block.lazyTypes(functionNode) = typeFun
      case _ =>
    }
  }

//  def searchReturnType(block: LazySymbolBlock, node: SyntaxTreeNode): LazyType = {
//    node match {
//      case returnNode: ReturnNode => block.lazyTypes(returnNode)
//      case _ => node.children.map(searchReturnType(block, _)).last
//    }
//  }
}
