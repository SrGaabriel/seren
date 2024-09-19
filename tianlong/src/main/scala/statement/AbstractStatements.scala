package me.gabriel.tianlong
package statement

import struct.{DragonType, ValueReference}

trait DragonStatement {
  val memoryDependencies: List[ValueReference]
  
  def valid: Boolean
  def llvm: String
}

trait TypedDragonStatement extends DragonStatement {
  val statementType: DragonType
}