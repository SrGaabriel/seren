package me.gabriel.tianlong

import factory.FunctionFactory
import struct.{ConstantReference, Dependency, DragonType}

class TianlongModule extends DragonModule {
  def createFunction(
                      name: String,
                      parameters: List[DragonType],
                      returnType: DragonType
                    ): FunctionFactory = {
    val function = new FunctionFactory(this, addFunction(name, parameters, returnType))
    function
  }

  def format(
            name: String,
            value: String,
            ): Dependency.Constant = {
    val constant = dependencies.find {
      case Dependency.Constant(_, depValue) => depValue match {
        case ConstantReference.SmartString(depStringValue) => depStringValue == value
        case _ => false
      }
      case _ => false
    }.map(_.asInstanceOf[Dependency.Constant])
    constant match {
      case Some(value) => value
      case None =>
        val newConstant = Dependency.Constant(name, ConstantReference.SmartString(value))
        dependencies += newConstant
        newConstant
    }
  }
  
  def createStruct(
              name: String,
              types: List[DragonType]
            ): Dependency.Struct = {
    val struct = Dependency.Struct(name, types)
    dependencies += struct
    struct
  }
}
