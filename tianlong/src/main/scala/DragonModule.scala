package me.gabriel.tianlong

import factory.FunctionFactory
import function.{DragonFunction, DragonFunctionBlock}
import struct.{Dependency, DragonType, MemoryReference}

import scala.collection.mutable

trait DragonModule {
  val dependencies: mutable.ListBuffer[Dependency] = mutable.ListBuffer()
  val functions: mutable.ListBuffer[DragonFunction] = mutable.ListBuffer()

  def addFunction(
    name: String,
    parameters: List[DragonType],
    returnType: DragonType
  ): DragonFunction = {
    val function = new DragonFunction(this, name, parameters.zipWithIndex.map((param, index) =>
      MemoryReference(
        register = index + 1,
        dragonType = param
      )
    ), returnType, mutable.LinkedHashMap("entry" -> new DragonFunctionBlock("entry")))
    functions += function
    function
  }

  def addFunction(function: DragonFunction): DragonFunction = {
    functions += function
    function
  }
}
