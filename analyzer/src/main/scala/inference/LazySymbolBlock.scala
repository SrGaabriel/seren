package me.gabriel.seren.analyzer
package inference

import scala.collection.mutable
import me.gabriel.seren.frontend.parser.tree.SyntaxTreeNode

class LazySymbolBlock(
                     module: String,
                     id: SyntaxTreeNode,
                     parent: Option[LazySymbolBlock],
                     children: mutable.ListBuffer[SymbolBlock]
                     ) extends SymbolBlock(module, parent, id, children) {
}