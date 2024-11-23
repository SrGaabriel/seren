package me.gabriel.seren.llvm
package util

import me.gabriel.tianlong.struct.DragonType

object PaddingSorter {
  def sortTypes(types: List[DragonType]): List[DragonType] = {
    types.sortBy(-_.bytes)
  }
}