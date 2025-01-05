package me.gabriel.tianlong
package function

import statement.DragonStatement
import struct.{DragonType, MemoryReference}

import scala.collection.mutable


class DragonFunction(
  val module: DragonModule,
  val name: String,
  val parameters: List[MemoryReference],
  var returnType: DragonType,
  val blocks: mutable.LinkedHashMap[String, DragonFunctionBlock]
) {
  val statements: mutable.ListBuffer[DragonStatement] = mutable.ListBuffer.empty
}
