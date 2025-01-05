package me.gabriel.tianlong
package factory

import function.DragonFunction
import statement.*
import struct.DragonType.Int8
import struct.*

import scala.collection.mutable

class FunctionFactory(
  val module: TianlongModule,
  val function: DragonFunction
) {
  val escapees: mutable.Set[MemoryReference] = mutable.Set.empty
  val assignments = mutable.Map.empty[MemoryReference, AssignStatement]
  var currentRegister: Int = function.parameters.size + 1

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

  def add(left: ValueReference, right: ValueReference): BinaryOpStatement = {
    binaryOp(left, right, BinaryOpType.Add)
  }

  def binaryOp(left: ValueReference, right: ValueReference, op: BinaryOpType): BinaryOpStatement = {
    BinaryOpStatement(left, right, op)
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
  ): BitcastStatement = {
    val format = module.format(name, value)
    bitcast(format, DragonType.ContextualPointer(format.dragonType), DragonType.ContextualPointer(Int8))
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

  def bitcast(
    value: ValueReference,
    fromType: DragonType,
    targetType: DragonType
  ): BitcastStatement = {
    BitcastStatement(value, fromType, targetType)
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

  def insertValue(
    struct: ValueReference,
    value: ValueReference,
    index: Int
  ): InsertValueStatement = {
    InsertValueStatement(struct, value, index)
  }
  
  def compareSignedIntegers(
    left: ValueReference,
    right: ValueReference,
    op: NumericalComparisonType
  ): SignedIntegerComparisonStatement =
    SignedIntegerComparisonStatement(left, right, op)

  def `return`(value: ValueReference): FunctionFactory = {
    statement(ReturnStatement(value))
  }

  def returnStatement(value: ValueReference): ReturnStatement = {
    ReturnStatement(value)
  }

  def assignAndLoadIfImmutable(value: TypedDragonStatement): Option[LoadStatement] = {
    value.dragonType match {
      case _: DragonType.Struct => None
      case _ => Some(LoadStatement(assign(value)))
    }
  }

  def nextMemoryReference(dragonType: DragonType): MemoryReference = {
    val memoryReference = MemoryReference(currentRegister, dragonType)
    currentRegister += 1
    memoryReference
  }
}
