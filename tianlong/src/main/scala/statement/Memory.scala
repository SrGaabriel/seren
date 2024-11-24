package me.gabriel.tianlong
package statement

import struct.{DragonType, MemoryReference, ValueReference, isPointer}

case class AssignStatement(
                            memory: MemoryReference, 
                            value: TypedDragonStatement
                          ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = List(memory).concat(value.memoryDependencies)

  override def valid: Boolean = true

  override def statementLlvm: String = s"${memory.llvm} = ${value.statementLlvm}"
}

case class StoreStatement(
                           value: ValueReference,
                           pointer: ValueReference
                         ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = List(value, pointer)

  override def valid: Boolean = pointer.dragonType.isPointer

  override def statementLlvm: String = s"store ${value.dragonType.llvm} ${value.llvm}, ${pointer.dragonType.llvm} ${pointer.llvm}"
}

case class BulkStoreStatement(
                               values: List[ValueReference],
                               pointer: ValueReference
                             ) extends DragonStatement {
  override val memoryDependencies: List[ValueReference] = values.concat(List(pointer))
  override def valid: Boolean = pointer.dragonType.isPointer

  private def onlyType = values.map(_.dragonType).distinct.head

  override def statementLlvm: String = {
    val valueLlvm = values.map(value => s"${value.dragonType.llvm} ${value.llvm}").mkString(", ")
    s"store ${onlyType.llvm} $valueLlvm, ${pointer.dragonType.llvm} ${pointer.llvm}"
  }
}

case class AllocateStatement(
                              allocationType: DragonType,
                              alignment: Int
                            ) extends TypedDragonStatement {
  override val memoryDependencies: List[ValueReference] = List.empty

  override def valid: Boolean = true
  override val statementType: DragonType = DragonType.ContextualPointer(allocationType)

  override def statementLlvm: String = s"alloca ${allocationType.llvm}, align $alignment"
}

case class LoadStatement(
                          pointer: ValueReference
                        ) extends TypedDragonStatement {
  override val memoryDependencies: List[ValueReference] = List(pointer)

  override def valid: Boolean = pointer.dragonType.isPointer

  override val statementType: DragonType = pointer.dragonType match {
    case DragonType.ContextualPointer(inner) => inner
    case regular => regular
  }
  override val dragonType: DragonType = statementType

  override def statementLlvm: String = s"load ${statementType.llvm}, ${pointer.dragonType.llvm} ${pointer.llvm}"
}

case class GetElementPointerStatement(
                                     struct: ValueReference,
                                     elementType: DragonType,
                                     index: ValueReference,
                                     total: Boolean = true,
                                      inBounds: Boolean = true
                                    ) extends TypedDragonStatement {
  override val memoryDependencies: List[ValueReference] = List(struct, index)

  override def valid: Boolean = true

  override val statementType: DragonType = DragonType.ContextualPointer(elementType)
  override val dragonType: DragonType = DragonType.ContextualPointer(elementType)

  private def originalType = struct.dragonType match {
    case DragonType.ContextualPointer(inner) => inner
    case regular => regular
  }
  private def pointerType = DragonType.ContextualPointer(originalType)

  override def statementLlvm: String =
    s"getelementptr" +
      s"${if (inBounds) " inbounds" else ""}" +
      s" ${originalType.llvm}, ${pointerType.llvm} ${struct.llvm}" +
      s"${if (total) ", i32 0" else ""}" +
      s", ${index.dragonType.llvm} ${index.llvm}"
}