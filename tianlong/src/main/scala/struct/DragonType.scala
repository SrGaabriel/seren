package me.gabriel.tianlong
package struct

sealed class DragonType(
                       val llvm: String,
                       val bytes: Int
                       )

object DragonType {
  case object UInt8 extends DragonType("u8", bytes = 1)
  case object UInt16 extends DragonType("u16", bytes = 2)
  case object UInt32 extends DragonType("u32", bytes = 4)
  case object UInt64 extends DragonType("u64", bytes = 8)
  case object Int8 extends DragonType("i8", bytes = 1)
  case object Int16 extends DragonType("i16", bytes = 2)
  case object Int32 extends DragonType("i32", bytes = 4)
  case object Int64 extends DragonType("i64", bytes = 8)
  case object Float32 extends DragonType("f32", bytes = 4)
  case object Float64 extends DragonType("f64", bytes = 8)
  case object Void extends DragonType("void", bytes = 0)

  case class Vararg(base: Option[DragonType]) extends DragonType("...", bytes = -1)
  case class Custom(name: String) extends DragonType(name, bytes = -1)

  case class ContextualPointer(innerType: DragonType) extends
    DragonType(s"${innerType.llvm}*", bytes=innerType.bytes)

  case class Array(
                  innerType: DragonType,
                  size: Int
                  ) extends DragonType(
    llvm = s"[$size x ${innerType.llvm}]",
    bytes = innerType.bytes * size
  )

  case class Struct(
                      name: String,
                      alignment: Int,
                    ) extends DragonType(
                      llvm = s"%$name",
                      bytes = alignment
                   )

  case object StaticPointer extends DragonType("ptr", bytes = 1)
}

extension (dragonType: DragonType) {
  def isPointer: Boolean = dragonType match {
    case _: DragonType.ContextualPointer => true
    case DragonType.StaticPointer => true
    case _ => false
  }
}
