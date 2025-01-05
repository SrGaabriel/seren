package me.gabriel.tianlong
package statement

import function.DragonFunctionBlock
import struct.{DragonType, NumericalComparisonType, ValueReference}

case class BranchStatement(
  block: DragonFunctionBlock
) extends TypedDragonStatement {
  override val statementType: DragonType = DragonType.Void
  override val memoryDependencies: List[ValueReference] = List.empty

  override def valid: Boolean = true

  override def statementLlvm: String = s"br label %${block.label}"
}

case class ConditionalBranchStatement(
  condition: ValueReference,
  trueBlock: DragonFunctionBlock,
  falseBlock: DragonFunctionBlock
) extends TypedDragonStatement {
  override val statementType: DragonType = DragonType.Void
  override val memoryDependencies: List[ValueReference] = List(condition)

  override def valid: Boolean = condition.dragonType == DragonType.Int1

  override def statementLlvm: String = s"br i1 ${condition.llvm}, label %${trueBlock.label}, label %${falseBlock.label}"
}

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