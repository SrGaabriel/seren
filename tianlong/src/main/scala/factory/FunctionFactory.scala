package me.gabriel.tianlong
package factory

import function.DragonFunction
import statement.{AddStatement, AllocateStatement, AssignStatement, DragonStatement, ReturnStatement, StoreStatement, TypedDragonStatement}
import struct.{MemoryReference, ValueReference}

class FunctionFactory(
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

  def allocate(allocationType: ValueReference, alignment: Int): AllocateStatement = {
    AllocateStatement(allocationType, alignment)
  }
  
  def add(left: ValueReference, right: ValueReference): AddStatement = {
    AddStatement(left, right)
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

  def `return`(value: ValueReference): Unit = {
    statement(ReturnStatement(value))
  }
}
