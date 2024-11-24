package me.gabriel.seren.llvm
package util

def convertControlChars(input: String): String = {
  input
    .replace("\\n", "\n")
    .replace("\\t", "\t")
    .replace("\\r", "\r")
    .replace("\\b", "\b")
    .replace("\\f", "\f")
    .replace("\\\\", "\\")
    .replace("\\\"", "\"")
}
