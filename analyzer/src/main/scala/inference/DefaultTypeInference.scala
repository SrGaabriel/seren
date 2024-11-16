package me.gabriel.seren.analyzer
package inference

import external.ModuleManager

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.{StructDeclarationNode, *}

class DefaultTypeInference extends TypeInference {
  private var typeVarCounter = 0
  private def newTypeVar(): LazyType = TypeVariable(s"t${typeVarCounter += 1; typeVarCounter}")

  override def traverseBottomUp(
                                 module: ModuleManager,
                                 block: LazySymbolBlock,
                                 node: SyntaxTreeNode
                               ): Unit = {
    val actualBlock = node match {
      case function: FunctionDeclarationNode =>
        val actualType = resolveThis(block, function.returnType)
        function.returnType = actualType
        module.addLocalFunction(
          name = function.name,
          params = function.parameters.map(p => p.nodeType),
          returnType = actualType
        )
        block.createChild(function)
      case struct: StructDeclarationNode =>
        module.addStruct(
          name = struct.name,
          fields = struct.fields.map(f => f.nodeType)
        )
        block.createChild(struct)
      case _ => block
    }

    node.children.foreach(traverseBottomUp(module, actualBlock, _))

    node match {
      case typedNode: TypedSyntaxTreeNode => processTypedNode(
        block = actualBlock,
        node = typedNode
      )
      case _ =>
    }
  }

  override def processTypedNode(
                                 block: LazySymbolBlock,
                                 node: TypedSyntaxTreeNode
                               ): LazyType = {
    node match {
      case functionNode: FunctionDeclarationNode =>
        val paramTypes = functionNode.parameters.map(_ => newTypeVar())

        val typeFun = TypeFunction(
          from = paramTypes,
          to = TypeLiteral(node.nodeType)
        )
        block.lazyDefine(functionNode, typeFun)
      case referenceNode: ReferenceNode =>
        block.lazyDefine(referenceNode, TypeVariable(referenceNode.name))
      case FunctionParameterNode(_, name, nodeType) =>
        val actualType = resolveThis(block, nodeType)
        block.lazyDefine(node, TypeLiteral(actualType))
        block.lazyRegisterSymbol(name, TypeLiteral(actualType))
      case assignmentNode: AssignmentNode =>
        val bodyType = processTypedNode(block, assignmentNode.value)
        block.lazyDefine(assignmentNode, bodyType)
        block.lazyRegisterSymbol(assignmentNode.name, bodyType)
      case callNode: FunctionCallNode =>
        block.lazyDefine(callNode, TypeCall(callNode.name, callNode.arguments.map(
          argument => processTypedNode(block,argument)
        )))
      case _ =>
        if (node.nodeType == Type.Unknown) {
          println(s"Warning: registering unknown typed node $node")
        }
        block.lazyDefine(node, TypeLiteral(node.nodeType))
    }
  }

  def resolveThis(block: LazySymbolBlock, typ: Type): Type = {
    typ match {
      case Type.UnknownThis => block.searchStruct().map(_.name).map(Type.Struct.apply).getOrElse(Type.UnknownThis)
      case _ => typ
    }
  }

//  def searchReturnType(block: LazySymbolBlock, node: SyntaxTreeNode): LazyType = {
//    node match {
//      case returnNode: ReturnNode => block.lazyTypes(returnNode)
//      case _ => node.children.map(searchReturnType(block, _)).last
//    }
//  }
}
