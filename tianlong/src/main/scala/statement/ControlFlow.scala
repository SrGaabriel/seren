package me.gabriel.tianlong
package statement

import struct.{DragonType, ValueReference}

case class ReturnStatement(
                            value: ValueReference
                          ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = List(value)

  override def valid: Boolean = true

  override def statementLlvm: String = s"ret ${value.dragonType.llvm} ${value.llvm}"
}

case class CallStatement(
                          name: String,
                          statementType: DragonType,
                          arguments: List[ValueReference]
                        ) extends TypedDragonStatement {
  override val memoryDependencies: List[ValueReference] = arguments

  override def valid: Boolean = true

  override def statementLlvm: String = {
    val args = arguments.map(_.dragonType.llvm).zip(arguments.map(_.llvm)).map((dragonType, llvm) => s"$dragonType $llvm").mkString(", ")
    s"call ${statementType.llvm} @$name($args)"
  }
}