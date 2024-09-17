package me.gabriel.seren.analyzer
package inference

import scala.collection.mutable
import me.gabriel.seren.frontend.parser.tree.{FunctionDeclarationNode, SyntaxTreeNode}

import scala.language.implicitConversions

class LazySymbolBlock(
                     module: String,
                     id: SyntaxTreeNode,
                     parent: Option[LazySymbolBlock],
                     children: mutable.ListBuffer[SymbolBlock]
                     ) extends SymbolBlock(module, parent, id, children) {
  val lazyDefinitions: mutable.Map[SyntaxTreeNode, LazyType] = mutable.Map[SyntaxTreeNode, LazyType]()
  val lazySymbols: mutable.Map[String, LazyType] = mutable.Map[String, LazyType]()
  
  override def createChild(id: SyntaxTreeNode): LazySymbolBlock = {
    val child = new LazySymbolBlock(module, id, Some(this), mutable.ListBuffer())
    children += child
    child
  }

  def lazyDefine(node: SyntaxTreeNode, lazyType: LazyType): LazyType = {
    lazyDefinitions(node) = lazyType
    lazyType
  }
  
  def lazyRegisterSymbol(name: String, lazyType: LazyType): LazyType = {
    lazySymbols(name) = lazyType
    lazyType
  }
  
  def prettyPrintLazyTypes(): Unit = {
    def printBlock(block: LazySymbolBlock, indent: Int): Unit = {
      println(">  " * indent + block.id + s" => ${block.lazyDefinitions.size} types")
      block.lazyDefinitions.foreach { case (node, lazyType) =>
        println("  " * (indent + 1) + s"$node -> $lazyType")
      }
      block.children.foreach(child => printBlock(child, indent + 1))
    }
    printBlock(this, 0)
  }
}

object LazySymbolBlock {
  implicit def toLazySymbolBlock(block: SymbolBlock): LazySymbolBlock = {
    block match
      case block1: LazySymbolBlock => block1
      case _ => new LazySymbolBlock(block.module, block.id, block.parent.map(toLazySymbolBlock), block.children)
  }
}