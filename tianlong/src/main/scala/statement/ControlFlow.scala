package me.gabriel.tianlong
package statement

import struct.ValueReference

case class ReturnStatement(
                            value: ValueReference
                          ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = List(value)

  override def valid: Boolean = true

  override def statementLlvm: String = s"ret ${value.llvm}"
}