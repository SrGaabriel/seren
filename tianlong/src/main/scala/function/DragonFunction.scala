package me.gabriel.tianlong
package function

import struct.{DragonType, MemoryReference}

class DragonFunction(
                      val module: DragonModule,
                      val name: String,
                      val parameters: List[MemoryReference],
                      val returnType: DragonType
                    ) {
  val statements = List.empty[statement.DragonStatement]
}
