package me.gabriel.seren.analyzer
package external

import me.gabriel.seren.frontend.parser.Type

import scala.collection.mutable.ListBuffer

class ModuleManager(val directive: Directive) {
  val packages: ListBuffer[Package] = ListBuffer()
  val local: ListBuffer[Package] = ListBuffer()

  def importPackage(`package`: Package): Unit = {
    packages += `package`
  }

  def addLocalFunction(
    name: String,
    params: List[Type],
    returnType: Type
  ): Unit =
    local += Package.Function(
      name = name,
      directive = directive,
      parameters = params,
      returnType = returnType
    )

  def addStruct(
    name: String,
    fields: Map[String, Type]
  ): Unit = {
    importPackage(
      Package.Struct(
        name = name,
        directive = directive,
        fields = fields
      )
    )
  }

  def searchFunction(name: String): Option[Package.Function] = {
    local.collectFirst {
      case function: Package.Function
        if function.name == name
      => function
    } orElse
    packages.collectFirst {
      case function: Package.Function
        if function.name == name
      => function
    }
  }

  def searchStruct(name: String): Option[Package.Struct] = {
    local.collectFirst {
      case struct: Package.Struct
        if struct.name == name
      => struct
    } orElse
    packages.collectFirst {
      case struct: Package.Struct
        if struct.name == name
      => struct
    }
  }
}
