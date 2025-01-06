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

  def linkLlFileToExecutable(output: String, llFiles: String*): Int =
    val seq = List("clang", "-o", output) ++ llFiles
    val process = new ProcessBuilder(seq: _*)
      .inheritIO()
      .start()
    process.waitFor()

  def readFile(name: String): String = {
    val file = new java.io.File(name)
    val source = scala.io.Source.fromFile(file)
    source.mkString
  }

  def runExecutable(name: String): Int = {
    val process = new ProcessBuilder(s"./$name")
      .inheritIO()
      .start()
    process.waitFor()
  }
}
