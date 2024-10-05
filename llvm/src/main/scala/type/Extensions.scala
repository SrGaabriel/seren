package me.gabriel.seren.llvm
package `type`

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.tianlong.struct.DragonType

extension (serenType: Type) {
  def dragon: DragonType = serenType match {
    case Type.Int => DragonType.Int32
    case Type.Void => DragonType.Void
    case Type.String => DragonType.ContextualPointer(DragonType.Int8)
    case _ => throw new Exception(s"Unsupported LLVM type $serenType")
  }
}