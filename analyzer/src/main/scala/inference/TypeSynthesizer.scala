package me.gabriel.seren.analyzer
package inference

object TypeSynthesizer {
  def assembleTypes(root: LazySymbolBlock): Unit = {
    root.lazyDefinitions.values.foreach {
      case TypeVariable(name) => {

      }
    }

    root.children.foreach(child => assembleTypes(child))
  }
}
