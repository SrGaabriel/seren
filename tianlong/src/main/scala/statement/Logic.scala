package me.gabriel.tianlong
package statement

import struct.{DragonType, NumericalComparisonType, ValueReference}

case class SignedIntegerComparisonStatement(
  left: ValueReference,
  right: ValueReference,
  comparison: NumericalComparisonType
) extends TypedDragonStatement {
  override val statementType: DragonType = DragonType.Int1
  val comparisonType: DragonType = left.dragonType

  override def valid: Boolean = left.dragonType == right.dragonType
  override val memoryDependencies: List[ValueReference] = List(left, right)

  override def statementLlvm: String =
    val op = comparison match
      case NumericalComparisonType.Equal => "eq"
      case NumericalComparisonType.NotEqual => "ne"
      case NumericalComparisonType.LessThan => "slt"
      case NumericalComparisonType.LessThanOrEqual => "sle"
      case NumericalComparisonType.GreaterThan => "sgt"
      case NumericalComparisonType.GreaterThanOrEqual => "sge"
    s"icmp $op ${comparisonType.llvm} ${left.llvm}, ${right.llvm}"
}