package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.{SyntaxTreeNode, TypedSyntaxTreeNode}

import scala.collection.mutable

object TypeSynthesizer {
  def updateNodeTypes(block: LazySymbolBlock, node: SyntaxTreeNode, substitutions: mutable.Map[String, LazyType]): Unit = {
    node match {
      case typedNode: TypedSyntaxTreeNode =>
        val inferredLazyType = block.lazyDefinitions(typedNode)
        val resolvedLazyType = resolveType(inferredLazyType, substitutions)
        val finalType = lazyTypeToType(block, resolvedLazyType)

        typedNode.nodeType = finalType
      case _ =>
    }
  }

  def lazyTypeToType(block: LazySymbolBlock, lazyType: LazyType): Type = {
    lazyType match {
      case TypeLiteral(actualType) => actualType
      case TypeFunction(from, to) =>
        lazyTypeToType(block, to)
      case TypeVariable(name) =>
        lazyTypeToType(block, block.lazySymbols(name))
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

      case TypeLiteral(_) =>
        lazyType
    }
  }

}
