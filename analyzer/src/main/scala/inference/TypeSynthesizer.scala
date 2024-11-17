package me.gabriel.seren.analyzer
package inference

import external.ModuleManager

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.Type.{UnknownIdentifier, UnknownThis}
import me.gabriel.seren.frontend.parser.tree.{NumericNode, SyntaxTreeNode, TypedSyntaxTreeNode}

import scala.annotation.tailrec
import scala.collection.mutable

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
        updateNodeTypes(module, block, node, mutable.Map())
      }
    }

    updateBlockTypes(rootBlock)
  }

  def updateNodeTypes(
                       module: ModuleManager,
                       block: LazySymbolBlock,
                       node: SyntaxTreeNode,
                       substitutions: mutable.Map[String, LazyType]
                     ): Unit = {
    node match {
      case typedNode: TypedSyntaxTreeNode =>
        val inferredLazyType = block.lazyDefinitions(typedNode)
        val resolvedLazyType = resolveType(inferredLazyType, substitutions)
        val finalType = lazyTypeToType(module, block, resolvedLazyType)

        if (finalType == Type.Unknown && typedNode.isInstanceOf[NumericNode]) {
          typedNode.nodeType = Type.Int
          return
        } else if (finalType == Type.Unknown) {
          throw new RuntimeException(s"Could not resolve type for node $node")
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
        lazyTypeToType(module, block, to)
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

  def resolveType(lazyType: LazyType, substitutions: mutable.Map[String, LazyType]): LazyType = {
    lazyType match {
      case TypeVariable(name) =>
        substitutions.get(name) match {
          case Some(resolvedType) => resolveType(resolvedType, substitutions)
          case None => lazyType
        }

      case TypeFunction(from, to) =>
        TypeFunction(
          from.map(t => resolveType(t, substitutions)),
          resolveType(to, substitutions)
        )

      case _ => lazyType
    }
  }
}
