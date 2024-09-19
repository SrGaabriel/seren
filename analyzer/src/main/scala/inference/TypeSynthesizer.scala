package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.analyzer.external.ModuleManager
import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.{AssignmentNode, SyntaxTreeNode, TypedSyntaxTreeNode}

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
        println(s"At block ${block.id} searching for ${typedNode.hashCode()} at ${block.lazyDefinitions.map((key, value) => key.hashCode())}")
        val inferredLazyType = block.lazyDefinitions(typedNode)
        val resolvedLazyType = resolveType(inferredLazyType, substitutions)
        val finalType = lazyTypeToType(module, block, resolvedLazyType)

        typedNode.nodeType = finalType
      case _ =>
    }
  }

  @tailrec
  def lazyTypeToType(
                      module: ModuleManager,
                      block: LazySymbolBlock,
                      lazyType: LazyType
                    ): Type = {
    lazyType match {
      case TypeLiteral(actualType) => actualType
      case TypeFunction(from, to) =>
        lazyTypeToType(module, block, to)
      case TypeVariable(name) =>
        lazyTypeToType(module, block, block.lazySymbols(name))
      case TypeCall(name, params) =>
        val functionPackage = module.searchFunction(name)
        println(s"Function: ${functionPackage} found at for $name ${module.packages}")
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
