package me.gabriel.seren.analyzer
package inference

import external.ModuleManager
import LazyType.*

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.*
import me.gabriel.seren.logging.LogLevel
import me.gabriel.seren.logging.tracing.Traceable

import scala.collection.{immutable, mutable}

class HardTypeInference extends TypeInference, Traceable {
  override def traverseBottomUp(
    module: ModuleManager,
    block: LazySymbolBlock,
    node: SyntaxTreeNode
  ): Unit = {
    val actualBlock = node match {
      case function: FunctionDeclarationNode =>
        module.addLocalFunction(
          name = function.name,
          params = function.parameters.map(p => p.nodeType),
          returnType = function.nodeType.returnType
        )
        block.createChild(function)
      case struct: StructDeclarationNode =>
        val fields = immutable.ListMap(struct.fields.map(f => f.name -> f.nodeType).to(mutable.LinkedHashMap).toSeq: _*)
        module.addStruct(
          name = struct.name,
          fields = fields
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
    def getChildType(child: TypedSyntaxTreeNode) = block.lazyDefinitions(child)
    node match {
      case functionNode: FunctionDeclarationNode =>
        block.lazyDefine(functionNode, TypeFunction(
          functionNode.parameters.map(getChildType),
          TypeLiteral(functionNode.nodeType.returnType)
        ))

      case referenceNode: ReferenceNode =>
        block.lazyDefine(referenceNode, TypeVariable(referenceNode.name))

      case structAccessNode: StructFieldAccessNode =>
        val structType = getChildType(structAccessNode.struct)
        block.lazyDefine(structAccessNode, TypeAccess(structType, structAccessNode.fieldName))

      case FunctionParameterNode(_, name, nodeType) =>
        block.lazyDefine(node, TypeLiteral(nodeType))
        block.lazyRegisterSymbol(name, TypeLiteral(nodeType))

      case assignmentNode: AssignmentNode =>
        val bodyType = getChildType(assignmentNode.value)
        block.lazyDefine(assignmentNode, bodyType)
        block.lazyRegisterSymbol(assignmentNode.name, bodyType)

      case callNode: FunctionCallNode =>
        block.lazyDefine(callNode, TypeCall(callNode.name, callNode.arguments.map(getChildType)))

      case _ =>
        if (node.nodeType == Type.Unknown) {
          log(LogLevel.WARNING, s"Registering unknown typed node $node")
        }
        block.lazyDefine(node, TypeLiteral(node.nodeType))
    }
  }
}
