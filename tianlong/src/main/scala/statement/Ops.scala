package me.gabriel.tianlong
package statement

import struct.{DragonType, ValueReference}

case class AddStatement(
                         left: ValueReference, 
                         right: ValueReference
                        ) extends TypedDragonStatement {
  override val memoryDependencies: List[ValueReference] = List(left, right)
  override val statementType: DragonType = left.dragonType

  override def valid: Boolean = left.dragonType == right.dragonType

  override def statementLlvm: String = s"add ${statementType.llvm} ${left.llvm}, ${right.llvm}"
}