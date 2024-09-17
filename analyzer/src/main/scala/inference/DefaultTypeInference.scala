package me.gabriel.seren.analyzer
package inference

import scala.collection.mutable
import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.{AssignmentNode, FunctionDeclarationNode, ReferenceNode, ReturnNode, SyntaxTreeNode, TypedSyntaxTreeNode}

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

  override def processTypedNode(block: LazySymbolBlock, node: TypedSyntaxTreeNode): LazyType = {
    node match {
      case functionNode: FunctionDeclarationNode =>
        val paramTypes = functionNode.parameters.map(_ => newTypeVar())

        val typeFun = TypeFunction(
          from = paramTypes,
          to = TypeLiteral(node.nodeType)
        )
        println(s"Function: (${block.id}, $typeFun)")
        block.lazyDefine(functionNode, typeFun)
      case referenceNode: ReferenceNode =>
        block.lazyDefine(referenceNode, TypeVariable(referenceNode.name))
      case assignmentNode: AssignmentNode =>
        val bodyType = block.lazyDefinitions(assignmentNode.value)
        block.lazyDefine(assignmentNode, bodyType)
        block.lazyRegisterSymbol(assignmentNode.name, bodyType)
      case _ => {
        if (node.nodeType == Type.Unknown) {
          println(s"Warning: registering unknown typed node $node")
        }
        block.lazyDefine(node, TypeLiteral(node.nodeType))
      }
    }
  }

//  def searchReturnType(block: LazySymbolBlock, node: SyntaxTreeNode): LazyType = {
//    node match {
//      case returnNode: ReturnNode => block.lazyTypes(returnNode)
//      case _ => node.children.map(searchReturnType(block, _)).last
//    }
//  }
}
