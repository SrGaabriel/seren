package me.gabriel.seren.compiler
package formatter

import me.gabriel.seren.logging.{LogLevel, SerenLogger}
import me.gabriel.seren.logging.effect.ConsoleColors

case class RowInfo(content: String, relativeIndex: Int, number: Int)

case class Mark(position: Int, value: String)

trait SyntaxTreeNode {
  def mark: Mark
  def getChildren: Seq[SyntaxTreeNode]
}

def printError(
  logger: SerenLogger,
  fileName: String,
  code: String,
  prefix: String,
  start: Int,
  end: Int,
  message: String
): Unit = {
  val length = end - start

  def trimIndentReturningWidth(str: String): (String, Int) = {
    val trimmed = str.replaceFirst("^\\s+", "")
    (trimmed, str.length - trimmed.length)
  }

  findRowOfIndex(code.split("\n"), start) match {
    case Some(rowInfo) =>
      val (contentTrim, trimWidth) = trimIndentReturningWidth(rowInfo.content)
      val relativeStart = rowInfo.relativeIndex - trimWidth
      val relativeEnd = math.min(
        rowInfo.relativeIndex + length - trimWidth,
        contentTrim.length
      )

      val highlightedContent = {
        val textToHighlight = contentTrim.substring(relativeStart, relativeEnd)
        val highlighted = ConsoleColors.RED.paint(textToHighlight)
        contentTrim.patch(relativeStart, highlighted, relativeEnd - relativeStart)
      }

      val positionIndicator = " " * relativeStart +
        "^" * (relativeEnd - relativeStart + 1)

      println(Seq(
        s"""$fileName:${rowInfo.number}:${relativeStart + 1}: ${Console.BOLD}${ConsoleColors.RED}error: ${Console.RESET}${Console.BOLD}[$prefix]${ConsoleColors.READABLE_GRAY} $message""",
        "|",
        s"| row: $highlightedContent",
        s"| pos: $positionIndicator"
      ).mkString("\n"))

    case None =>
      throw new IllegalStateException("Error while finding the line of the error")
  }
}

def printError(
  logger: SerenLogger,
  fileName: String,
  code: String,
  prefix: String,
  node: SyntaxTreeNode,
  message: String
): Unit = {
  printError(
    logger = logger,
    fileName = fileName,
    code = code,
    prefix = prefix,
    start = node.mark.position,
    end = findEndOfNode(node),
    message = message
  )
}

private def findEndOfNode(node: SyntaxTreeNode): Int = {
  if (node.getChildren.isEmpty) {
    node.mark.position + node.mark.value.length
  } else {
    node.getChildren.map(findEndOfNode).max
  }
}


def findRowOfIndex(rows: Seq[String], index: Int): Option[RowInfo] = {
  val content = rows.mkString("\n")

  if (index < 0 || index >= content.length) {
    None
  } else {
    // Find the start of the row containing the index
    val rowStartIndex = content.lastIndexOf('\n', index)
    val rowEndIndex = content.indexOf('\n', index)

    // Determine the end index of the row, or use the end of the content if it's the last row
    val actualRowEndIndex = if (rowEndIndex == -1) content.length else rowEndIndex

    // Extract the row content
    val rowContent = content.substring(rowStartIndex + 1, actualRowEndIndex)

    // Calculate the relative index in the row
    val relativeIndexInRow = index - (rowStartIndex + 1)

    val rowNumber = content.substring(0, index).count(_ == '\n') + 1

    Some(RowInfo(rowContent, relativeIndexInRow, rowNumber))
  }
}

def replaceAtIndex(original: String, index: Int, length: Int, replacement: String): String = {
  if (index < 0 || index >= original.length) {
    throw new IndexOutOfBoundsException(
      s"Index $index is out of bounds for string of length ${original.length}"
    )
  }

  val before = original.substring(0, index)
  val after = original.substring(index + length)
  before + replacement + after
}

def trimIndentReturningWidth(str: String): (String, Int) = {
  val width = str.indexWhere(!_.isWhitespace) match {
    case -1 => str.length
    case n => n
  }
  str.substring(width) -> width
}
