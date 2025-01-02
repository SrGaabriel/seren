package me.gabriel.seren.compiler
package io

class CompilerIoHandler {
  def writeFile(name: String, content: String): java.io.File = {
    val file = new java.io.File(name)
    val writer = new java.io.PrintWriter(file)
    writer.write(content)
    writer.close()
    file
  }

  def linkLlFileToExecutable(llFile: String, output: String): Unit = {
    val process = new ProcessBuilder("clang", llFile, "-o", output)
      .inheritIO()
      .start()
    process.waitFor()
  }

  def readFile(name: String): String = {
    val file = new java.io.File(name)
    val source = scala.io.Source.fromFile(file)
    source.mkString
  }
}
