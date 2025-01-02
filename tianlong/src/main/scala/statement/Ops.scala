package me.gabriel.tianlong
package statement

import struct.{BinaryOpType, DragonType, ValueReference}

case class BinaryOpStatement(
  left: ValueReference,
  right: ValueReference,
  op: BinaryOpType
) extends TypedDragonStatement {
  override val memoryDependencies: List[ValueReference] = List(left, right)
  override val statementType: DragonType = left.dragonType

  override def valid: Boolean = left.dragonType == right.dragonType

  def floatingPoint: Boolean = statementType == DragonType.Float32 || statementType == DragonType.Float64

  override def statementLlvm: String = s"${op.getName(floatingPoint)} ${statementType.llvm} ${left.llvm}, ${right.llvm}"
}