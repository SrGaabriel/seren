package me.gabriel.tianlong
package struct

trait ValueReference {
  val dragonType: DragonType

  def llvm: String
}

object EmptyValue extends ValueReference {
  override val dragonType: DragonType = DragonType.Void

  override def llvm: String = ""
}

case class MemoryReference(
  register: Int,
  dragonType: DragonType
) extends ValueReference {
  override def llvm: String = "%reg_" + register
}

sealed abstract class ConstantReference(
  val dragonType: DragonType
) extends ValueReference

object ConstantReference {
  case class Null(
    override val dragonType: DragonType
  ) extends ConstantReference(dragonType) {
    override def llvm: String = "null"
  }

  case class Number(
    number: String,
    override val dragonType: DragonType
  ) extends ConstantReference(dragonType) {
    override def llvm: String = number
  }

  case class Struct(
    fields: List[ValueReference],
    override val dragonType: DragonType.Struct
  ) extends ConstantReference(dragonType) {
    override def llvm: String = s"{ ${fields.map(f => s"${f.dragonType.llvm} ${f.llvm}").mkString(", ")} }"
  }

  case class SmartString(
    text: String,
  ) extends ConstantReference(DragonType.Array(
    innerType = DragonType.Int8,
    size = text.length + 1
  )) {
    override def llvm: String = s"c\"$text\\00\""
  }
}