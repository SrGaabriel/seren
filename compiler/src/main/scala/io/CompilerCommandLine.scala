package me.gabriel.seren.compiler
package io

class CompilerCommandLine(args: List[String]) {
  def parse(): CompilerCommandConfig = {
    val input = readIndependentArgument()
    val output = readOption("output")
    val llvmOnly = readFlag("llvm-only")
    val keepAll = readFlag("keep")

    new CompilerCommandConfig(
      input = input,
      output = output,
      llvmOnly = llvmOnly,
      keepAll = keepAll
    )
  }
  
  def readFlag(name: String): Boolean = {
    args.contains(s"--$name")
  }
  
  def readInlineOption(name: String): Option[String] = {
    args.find(_.startsWith(s"--$name=")).map(_.split("=").last)
  }
  
  private def readOption(name: String): Option[String] = {
    args.sliding(2).collectFirst {
      case List(s"-$flag", value) if flag == name => value
    }
  }
  
  private def readIndependentArgument(): String = {
    args.filterNot(_.startsWith("-")).mkString(" ")
  }
}

class CompilerCommandConfig(
  val input: String,
  val output: Option[String],
  val llvmOnly: Boolean,
  val keepAll: Boolean
) {
  def inputFile = input.split('.').head match {
    case name if name.endsWith(".sr") => name
    case other => other + ".sr"
  }
  
  def inputName = input.split('.') match {
    case Array(name, "sr") => Some(name)
    case _ => None
  }
  
  override def toString: String = {
    s"CompilerCommandConfig(input=$input, output=$output)"
  }
}