package me.gabriel.seren.llvm
package `type`

import util.IterableExtensions.IterableLCM

import me.gabriel.seren.frontend.parser.Type
import me.gabriel.tianlong.struct.DragonType

extension (serenType: Type) {
  def allocationDragon: DragonType = serenType match {
    case Type.Int => DragonType.Int32
    case Type.Void => DragonType.Void
    case Type.String => DragonType.Array(DragonType.Int8, 0)
    case Type.CType(name) => DragonType.Custom(name)
    case Type.Vararg(_) => DragonType.Vararg(None)
    case Type.Struct(name, fields) => DragonType.Struct(name, fields.values.map(_.allocationDragon.bytes).lcmOfIterable)
    case _ => throw new Exception(s"Unsupported LLVM type $serenType")
  }
  def referenceDragon: DragonType = serenType match {
    case Type.String => DragonType.ContextualPointer(DragonType.Int8)
    case Type.Struct(_, _) => DragonType.ContextualPointer(allocationDragon)
    case _ => allocationDragon
  }
}