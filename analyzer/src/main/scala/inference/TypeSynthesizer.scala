package me.gabriel.seren.analyzer
package inference

import external.ModuleManager

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.Type.{UnknownIdentifier, UnknownThis}
import me.gabriel.seren.frontend.parser.tree.{FunctionCallNode, NumericNode, StructInstantiationNode, SyntaxTreeNode, TypedSyntaxTreeNode}

object TypeSynthesizer {
  def updateTreeTypes(
                        module: ModuleManager,
                        rootBlock: LazySymbolBlock
                      ): Unit = {
    def updateBlockTypes(block: LazySymbolBlock): Unit = {
      block.children.foreach { case childBlock: LazySymbolBlock =>
        updateBlockTypes(childBlock)
      }

      block.lazyDefinitions.foreach { case (node, lazyType) =>
        updateNodeTypes(module, block, node)
      }
    }

    updateBlockTypes(rootBlock)
  }

  def updateNodeTypes(
                       module: ModuleManager,
                       block: LazySymbolBlock,
                       node: SyntaxTreeNode
                     ): Unit = {
    node match {
      case typedNode: TypedSyntaxTreeNode =>
        val inferredLazyType = block.lazyDefinitions(typedNode)
        val finalType = lazyTypeToType(module, block, inferredLazyType)

        if (finalType == Type.Unknown && typedNode.isInstanceOf[NumericNode]) {
          typedNode.nodeType = Type.Int
          return
        } else if (finalType == Type.Unknown) {
          throw new RuntimeException(s"Could not resolve type for node $node ($inferredLazyType)")
        }

        typedNode.nodeType = finalType
      case _ =>
    }
  }

  def lazyTypeToType(
                      module: ModuleManager,
                      block: LazySymbolBlock,
                      lazyType: LazyType
                    ): Type = {
    lazyType match {
      case TypeLiteral(actualType) =>
        actualType match {
          case UnknownThis => 
            block.searchStruct() match {
              case Some(struct) => Type.Struct(struct.name, struct.fields.map(f => f.name -> f.nodeType).toMap)
              case None => actualType
            }
          case UnknownIdentifier(identifier) =>
            module.searchStruct(identifier) match {
              case Some(struct) => Type.Struct(struct.name, struct.fields)
              case None => actualType
            }
          case _ => actualType
        }
      case TypeFunction(from, to) =>
        Type.Function(from.map(t => lazyTypeToType(module, block, t)), lazyTypeToType(module, block, to))
      case TypeAccess(structType, field) =>
        val struct = lazyTypeToType(module, block, structType)
        struct match {
          case Type.Struct(name, fields) =>
            fields.get(field) match {
              case Some(fieldType) => fieldType
              case None => Type.Unknown
            }
          case _ => Type.Unknown
        }
      case TypeVariable(name) =>
        lazyTypeToType(module, block, block.lazySymbols(name))
      case TypeCall(name, params) =>
        val functionPackage = module.searchFunction(name)
        functionPackage match {
          case Some(function) => function.returnType
          case None => Type.Unknown
        }
    }
  }

  def getExpectedTypes(
                       module: ModuleManager,
                       block: LazySymbolBlock,
                       node: SyntaxTreeNode
                     ): List[LazyType] = {
    node match {
      case call: FunctionCallNode => 
        module.searchFunction(call.name) match {
          case Some(function) => function.parameters.map(TypeLiteral.apply)
          case None => List.empty
        }
      case instantiation: StructInstantiationNode =>
        module.searchStruct(instantiation.structName) match {
          case Some(struct) => struct.fields.map(f => TypeLiteral(f._2)).toList
          case None => List.empty
        }
      case _ => List.empty
    }
  }
}
