package me.gabriel.tianlong
package factory

import function.{DragonFunction, DragonFunctionBlock}
import statement.DragonStatement

class BlockFactory(
  val module: TianlongModule,
  val function: DragonFunction,
  val functionFactory: FunctionFactory,
  val block: DragonFunctionBlock
) extends StatementHolder {
  override def addStatement(statement: DragonStatement): Any = {
    block.statements += statement
  }
}
