package me.gabriel.tianlong

import factory.FunctionFactory
import function.DragonFunction
import struct.{Dependency, DragonType, MemoryReference}

import scala.collection.mutable

trait DragonModule {
  val dependencies: mutable.ListBuffer[Dependency] = mutable.ListBuffer()
  val functions: mutable.ListBuffer[DragonFunction] = mutable.ListBuffer()

  def createFunction(
                      name: String,
                      parameters: List[DragonType],
                      returnType: DragonType
                    ): FunctionFactory = {
    val function = new FunctionFactory(addFunction(name, parameters, returnType))
    function
  }

  def addFunction(
                   name: String,
                   parameters: List[DragonType],
                   returnType: DragonType
                 ): DragonFunction = {
    val function = new DragonFunction(this, name, parameters.zipWithIndex.map((param, index) =>
      MemoryReference(
        register = index,
        dragonType = param
      )
    ), returnType)
    functions += function
    function
  }

  def addFunction(function: DragonFunction): DragonFunction = {
    functions += function
    function
  }
}
