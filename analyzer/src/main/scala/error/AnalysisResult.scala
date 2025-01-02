package me.gabriel.seren.analyzer
package error

import scala.collection.mutable

class AnalysisResult(
  val errors: mutable.ListBuffer[AnalysisError] = mutable.ListBuffer()
) {
  def error(error: AnalysisError): Unit = {
    errors += error
  }
}