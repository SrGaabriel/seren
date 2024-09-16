package me.gabriel.seren.analyzer

import me.gabriel.seren.frontend.parser.tree.{RootNode, SyntaxTreeNode}
import me.gabriel.seren.frontend.parser.Type

import scala.collection.mutable

class TypeEnvironment(val module: String, private val rootNode: RootNode) {
  val root = new SymbolBlock(module, None, rootNode)

  def createBlock(id: SyntaxTreeNode): SymbolBlock = {
    root.createChild(id)
  }
}

class SymbolBlock(
  val module: String,
  val parent: Option[SymbolBlock],
  val id: SyntaxTreeNode,
  val children: mutable.ListBuffer[SymbolBlock] = mutable.ListBuffer(),
) {

  private val symbols = mutable.Map[String, Type]()
  private val definitions = mutable.Map[String, SyntaxTreeNode]()

  def createChild(id: SyntaxTreeNode): SymbolBlock = {
    val child = new SymbolBlock(module, Some(this), id)
    children += child
    child
  }

  def surfaceSearchChild(id: SyntaxTreeNode): Option[SymbolBlock] = {
    children.find(_.id == id)
  }

  def declareSymbol(name: String, t: Type): Unit = {
    symbols += (name -> t)
  }

  def defineSymbol(name: String, node: SyntaxTreeNode): Unit = {
    definitions += (name -> node)
  }

  def lookupSymbol(name: String): Option[Type] = {
    symbols.get(name) match {
      case Some(t) => Some(t)
      case None => parent.flatMap(_.lookupSymbol(name))
    }
  }

  def lookupDefinition(name: String): Option[SyntaxTreeNode] = {
    definitions.get(name) match {
      case Some(node) => Some(node)
      case None => parent.flatMap(_.lookupDefinition(name))
    }
  }
}
