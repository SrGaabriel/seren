package me.gabriel.tianlong
package factory

import function.{DragonFunction, DragonFunctionBlock}
import statement.*
import struct.MemoryReference

import scala.collection.mutable

class FunctionFactory(
  val module: TianlongModule,
  val function: DragonFunction
) extends StatementHolder {
  val escapees: mutable.Set[MemoryReference] = mutable.Set.empty
  val assignments = mutable.Map.empty[MemoryReference, AssignStatement]
  var statementRegister: Int = function.parameters.size + 1
  var blockRegister: Int = 0

  override val functionFactory: FunctionFactory = this

  private var currentBlock: Option[DragonFunctionBlock] = None
  
  def getBlockForNextStatement: DragonFunctionBlock = currentBlock match
    case Some(block) => block
    case None => function.blocks.last._2

  def addStatement(statement: DragonStatement): Unit = {
    if function.blocks.nonEmpty then
      getBlockForNextStatement.statements += statement
    else
      function.statements += statement
  }
  
  def setBlock(block: DragonFunctionBlock): Unit = {
    currentBlock = Some(block)
  }
  
  def setBlockToEntry(): Unit = {
    currentBlock = Some(function.blocks.head._2)
  }
  
  def resetBlock(): Unit = {
    currentBlock = None
  }
  
  def addStatementToBlock(block: DragonFunctionBlock, statement: DragonStatement): Unit = {
    block.statements += statement
  }
  
  def addStatementToEntry(statement: DragonStatement): Unit = {
    function.blocks.head._2.statements += statement
  }
}
