package me.gabriel.seren.analyzer
package external

import me.gabriel.seren.frontend.parser.Type
import scala.collection.mutable.ListBuffer

class ModuleManager(val directive: Directive) {
  val packages: ListBuffer[Package] = ListBuffer()

  def importPackage(`package`: Package): Unit = {
    packages += `package`
  }

  def addLocalFunction(
                    name: String,
                    params: List[Type],
                    returnType: Type
                    ): Unit = {
    importPackage(
      Package.Function(
        name = name,
        directive = directive,
        parameters = params,
        returnType = returnType
      )
    )
  }
  
  def searchFunction(name: String): Option[Package.Function] = {
    packages.collectFirst {
      case function: Package.Function
        if function.name == name 
      => function
    }
  }
}
