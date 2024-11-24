package me.gabriel.tianlong
package statement

import struct.{DragonType, ValueReference}

class BitcastStatement(
                        value: ValueReference,
                        fromType: DragonType,
                        targetType: DragonType
                      ) extends TypedDragonStatement {
  override val memoryDependencies: List[ValueReference] = List(value)
  override val statementType: DragonType = targetType

  override def valid: Boolean = true

  override def statementLlvm: String = s"bitcast ${fromType.llvm} ${value.llvm} to ${targetType.llvm}"
}