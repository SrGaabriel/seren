package me.gabriel.tianlong
package factory

import function.DragonFunction
import statement.*
import struct.DragonType.Int8
import struct.{ConstantReference, DragonType, MemoryReference, ValueReference}
import scala.collection.mutable

class FunctionFactory(
                      val module: TianlongModule,
                      val function: DragonFunction
                     ) {
  val escapees: mutable.Set[MemoryReference] = mutable.Set.empty
  val assignments = mutable.Map.empty[MemoryReference, AssignStatement]
  var currentRegister: Int = math.max(function.parameters.size, 1)

  def statement(statement: DragonStatement): FunctionFactory = {
    if (!statement.valid) {
      throw new IllegalArgumentException(s"Invalid statement: $statement")
    }
    if (statement.isInstanceOf[CallStatement]) {
      escapees ++= statement.memoryDependencies.collect {
        case memoryReference: MemoryReference => memoryReference
      }
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

  def call(
            name: String,
            returnType: DragonType,
            arguments: List[ValueReference]
          ): CallStatement = {
    CallStatement(name, returnType, arguments)
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
    val memoryReference = nextMemoryReference(typedStatement.statementType)
    val assignment = assignStatement(memoryReference, typedStatement, constantOverride)
    assignments(memoryReference) = assignment
    statement(assignment)
    memoryReference
  }

  def assignStatement(reference: MemoryReference, typedStatement: TypedDragonStatement, constantOverride: Option[Boolean] = None): AssignStatement = {
    val constant = constantOverride.getOrElse(typedStatement match {
      // TODO: pure calls
      case _ => false
    })

    AssignStatement(reference, typedStatement)
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

  def returnStatement(value: ValueReference): ReturnStatement = {
    ReturnStatement(value)
  }
  
  def nextMemoryReference(dragonType: DragonType): MemoryReference = {
    val memoryReference = MemoryReference(currentRegister, dragonType)
    currentRegister += 1
    memoryReference
  }
}
