package me.gabriel.tianlong
package factory

import function.DragonFunction
import statement.*
import struct.DragonType.Int8
import struct.{ConstantReference, DragonType, MemoryReference, ValueReference}

class FunctionFactory(
                      val module: TianlongModule,
                      val function: DragonFunction
                     ) {
  var currentRegister: Int = math.max(function.parameters.size, 1)

  def statement(statement: DragonStatement): FunctionFactory = {
    if (!statement.valid) {
      throw new IllegalArgumentException(s"Invalid statement: $statement")
    }
    currentRegister += 1
    function.statements += statement
    this
  }

  def store(value: ValueReference, target: ValueReference): FunctionFactory = {
    statement(StoreStatement(value, target))
  }

  def bulkStore(values: List[ValueReference], target: ValueReference): FunctionFactory = {
    statement(BulkStoreStatement(values, target))
  }

  def allocate(allocationType: DragonType, alignment: Int): AllocateStatement = {
    AllocateStatement(allocationType, alignment)
  }
  
  def add(left: ValueReference, right: ValueReference): AddStatement = {
    AddStatement(left, right)
  }

  def useFormat(
              name: String,
              value: String,
            ): GetElementPointerStatement = {
    val format = module.format(name, value)
    getElementAt(
      format,
      Int8,
      ConstantReference.Number("0", DragonType.Int32),
      total = false,
      inBounds = false
    )
  }

  def assign(typedStatement: TypedDragonStatement, constantOverride: Option[Boolean] = None): MemoryReference = {
    val constant = constantOverride.getOrElse(typedStatement match {
      // TODO: pure calls
      case _ => false
    })

    val memoryReference = MemoryReference(currentRegister, typedStatement.statementType)
    val assignment = AssignStatement(memoryReference, typedStatement)
    statement(assignment)
    memoryReference
  }

  def getElementAt(
                    struct: ValueReference,
                    elementType: DragonType,
                    index: ValueReference,
                    total: Boolean = true,
                    inBounds: Boolean = true
                  ): GetElementPointerStatement =
    GetElementPointerStatement(struct, elementType, index, total, inBounds)

  def newString(text: String): MemoryReference = {
    val allocated = assign(allocate(DragonType.Int8, text.length + 1))
    // maps chars to their ascii values
    val constants = text.map(char => ConstantReference.Number(char.toInt.toString, DragonType.Int8)).toList
    bulkStore(constants, allocated)
    allocated
  }

  def `return`(value: ValueReference): FunctionFactory = {
    statement(ReturnStatement(value))
  }
}
