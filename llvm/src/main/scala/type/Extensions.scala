package me.gabriel.seren.llvm
package `type`

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.tianlong.struct.DragonType

extension (serenType: Type) {
  def allocationDragon: DragonType = serenType match {
    case Type.Int => DragonType.Int32
    case Type.Void => DragonType.Void
    case Type.String => DragonType.Array(DragonType.Int8, 0)
    case Type.CType(name) => DragonType.Custom(name)
    case Type.Vararg(_) => DragonType.Vararg(None)
    case Type.Struct(name) => DragonType.Struct(name)
    case _ => throw new Exception(s"Unsupported LLVM type $serenType")
  }
  def referenceDragon: DragonType = serenType match {
    case Type.String => DragonType.ContextualPointer(DragonType.Int8)
    case _ => allocationDragon
  }
}