package me.gabriel.seren.frontend
package struct

import scala.annotation.tailrec

class TokenStream(tokens: List[Token]):
  var index = 0

  def peek: Token = tokens(index)

  def peekNext: Token = tokens(index + 1)

  @tailrec final def peekValid(
    ignoreWhitespaces: Boolean = true,
    ignoreNewLines: Boolean = true
  ): Token = tokens(index) match {
    case Token(_, TokenKind.Whitespace, _) if ignoreWhitespaces =>
      index += 1
      peekValid(ignoreWhitespaces, ignoreNewLines)
    case Token(_, TokenKind.NewLine, _) if ignoreNewLines =>
      index += 1
      peekValid(ignoreWhitespaces, ignoreNewLines)
    case _ =>
      tokens(index)
  }

  def peekNextValid(
    ignoreWhitespaces: Boolean = true,
    ignoreNewLines: Boolean = true
  ): Token = {
    @tailrec def getFirstValidIndex(index: Int): Int = tokens(index) match {
      case Token(_, TokenKind.Whitespace, _) if ignoreWhitespaces =>
        getFirstValidIndex(index + 1)
      case Token(_, TokenKind.NewLine, _) if ignoreNewLines =>
        getFirstValidIndex(index + 1)
      case _ =>
        index
    }

    val firstValidIndex = getFirstValidIndex(index)
    val secondValidIndex = getFirstValidIndex(firstValidIndex + 1)
    tokens(secondValidIndex)
  }

  def next: Token =
    val token = peek
    index += 1
    token

  def nextValid(
    ignoreWhitespaces: Boolean = true,
    ignoreNewLines: Boolean = true
  ): Token =
    val token = peekValid(ignoreWhitespaces, ignoreNewLines)
    index += 1
    token

  def hasNext: Boolean = index < tokens.length

  override def toString: String = tokens.drop(index).mkString(" ")
end TokenStream
