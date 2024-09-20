package me.gabriel.tianlong
package factory

import function.DragonFunction
import statement.{DragonStatement, StoreStatement, TypedDragonStatement}

import me.gabriel.tianlong.struct.{MemoryReference, ValueReference}

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

  def allocate(allocationType: ValueReference, alignment: Int): MemoryReference = {
    statement(AllocateStatement(allocationType, alignment))
  }

  def assign(statement: TypedDragonStatement, constantOverride: Option[Boolean] = None): MemoryReference = {
    val constant = constantOverride.getOrElse(statement match {
      // TODO: pure calls
      case _ => false
    })
    
    val memoryReference = MemoryReference(currentRegister, statement.statementType)
  }
}
