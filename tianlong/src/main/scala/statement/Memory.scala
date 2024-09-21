package me.gabriel.tianlong
package statement

import struct.{MemoryReference, ValueReference, isPointer}

case class AssignStatement(
                            memory: MemoryReference, 
                            value: TypedDragonStatement
                          ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = List(memory).concat(value.memoryDependencies)

  override def valid: Boolean = true

  override def statementLlvm: String = s"%${memory.register} = ${value.llvm}"
}

case class StoreStatement(
                           value: ValueReference,
                           pointer: ValueReference
                         ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = List(value, pointer)

  override def valid: Boolean = pointer.dragonType.isPointer

  override def statementLlvm: String = s"store ${value.dragonType.llvm} ${value.llvm}, ${pointer.dragonType.llvm}* ${pointer.llvm}"
}

case class AllocateStatement(
                              allocationType: ValueReference,
                              alignment: Int
                            ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = List.empty

  override def valid: Boolean = true

  override def statementLlvm: String = s"alloca ${allocationType.dragonType.llvm}, align $alignment"
}