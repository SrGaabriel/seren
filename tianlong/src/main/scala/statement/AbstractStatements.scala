package me.gabriel.tianlong
package statement

import struct.{DragonType, ValueReference}

trait DragonStatement {
  val memoryDependencies: List[ValueReference]
  
  def valid: Boolean
  def statementLlvm: String
}

trait TypedDragonStatement extends DragonStatement, ValueReference {
  val statementType: DragonType

  override val dragonType: DragonType = statementType

  override def llvm: String = statementLlvm
}

extension (value: ValueReference) {
  def flattenValue: String = value match {
    case statement: TypedDragonStatement => s"(${statement.llvm})"
    case _ => value.llvm
  }
}