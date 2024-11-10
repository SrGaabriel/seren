package me.gabriel.tianlong
package transcriber

import function.DragonFunction
import struct.Dependency

class TianlongIrTranscriber extends DragonIrTranscriber {
  override def transcribe(module: DragonModule): String = {
    val dependencies = module.dependencies.map(transcribeDependency).mkString("\n")
    val functions = module.functions.map(transcribeFunction).mkString("\n")
    s"""
      |$dependencies
      |
      |$functions
      |""".stripMargin
  }

  def transcribeDependency(dependency: Dependency): String = {
    dependency match {
      case Dependency.Constant(name, value) =>
        s"@$name = unnamed_addr constant ${value.dragonType.llvm} ${value.llvm}"
      case Dependency.Function(name, returnType, parameters) =>
        s"declare ${returnType.llvm} @$name(${parameters.map(_.llvm).mkString(", ")})"
      case Dependency.Struct(name, fields) =>
        s"%$name = type { ${fields.map(_.llvm).mkString(", ")} }"
    }
  }

  def transcribeFunction(function: DragonFunction): String = {
    val sb = new StringBuilder
    sb.append(s"define ${function.returnType.llvm} @${function.name}(")
    sb.append(function.parameters.map(param => s"${param.dragonType.llvm} ${param.llvm}").mkString(", "))
    sb.append(") {\n")
    sb.append(function.statements.map(statement => s"  ${statement.statementLlvm}").mkString("\n"))
    sb.append("\n}")
    sb.toString()
  }
}
