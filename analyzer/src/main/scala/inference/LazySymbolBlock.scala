package me.gabriel.seren.analyzer
package inference

import scala.collection.mutable
import me.gabriel.seren.frontend.parser.tree.SyntaxTreeNode

import scala.language.implicitConversions

class LazySymbolBlock(
                     module: String,
                     id: SyntaxTreeNode,
                     parent: Option[LazySymbolBlock],
                     children: mutable.ListBuffer[SymbolBlock]
                     ) extends SymbolBlock(module, parent, id, children) {
  val lazyTypes = mutable.Map[SyntaxTreeNode, LazyType]()
  
  override def createChild(id: SyntaxTreeNode): LazySymbolBlock = {
    val child = new LazySymbolBlock(module, id, Some(this), mutable.ListBuffer())
    children += child
    child
  }
  
  // print this block and its children's lazy types
  def prettyPrintLazyTypes(): Unit = {
    def printBlock(block: LazySymbolBlock, indent: Int): Unit = {
      println(">  " * indent + block.id)
      block.lazyTypes.foreach { case (node, lazyType) =>
        println("  " * (indent + 1) + s"$node -> $lazyType")
      }
      block.children.foreach(child => printBlock(child, indent + 1))
    }
    printBlock(this, 0)
  }
}

object LazySymbolBlock {
  implicit def toLazySymbolBlock(block: SymbolBlock): LazySymbolBlock = {
    new LazySymbolBlock(block.module, block.id, block.parent.map(toLazySymbolBlock), block.children)
  }
}