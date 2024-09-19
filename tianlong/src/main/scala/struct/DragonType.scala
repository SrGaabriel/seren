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
  case object Int8 extends DragonType("int8", bytes = 1)
  case object Int16 extends DragonType("int16", bytes = 2)
  case object Int32 extends DragonType("int32", bytes = 4)
  case object Int64 extends DragonType("int64", bytes = 8)
  case object Float32 extends DragonType("f32", bytes = 4)
  case object Float64 extends DragonType("f64", bytes = 8)

  case class ContextualPointer(innerType: DragonType) extends
    DragonType(s"${innerType.llvm}*", bytes=innerType.bytes)

  case class Array(
                  innerType: DragonType,
                  size: Int
                  ) extends DragonType(
    llvm = s"[$size x ${innerType.llvm}]",
    bytes = innerType.bytes * size
  )

  case object StaticPointer extends DragonType("ptr", bytes = 1)
}
