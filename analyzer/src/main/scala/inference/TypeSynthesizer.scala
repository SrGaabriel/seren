package me.gabriel.seren.analyzer
package inference

import me.gabriel.seren.frontend.parser.tree.SyntaxTreeNode

trait TypeSynthesizer {
  def union(left: LazyType, right: LazyType): Option[LazyType]
}
