package me.gabriel.tianlong
package function

import statement.DragonStatement

import scala.collection.mutable

class DragonFunctionBlock(
  val label: String,
) {
  val statements: mutable.ListBuffer[DragonStatement] = mutable.ListBuffer.empty
}