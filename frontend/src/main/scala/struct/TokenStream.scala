package me.gabriel.seren.frontend
package struct

import scala.annotation.tailrec

class TokenStream(tokens: List[Token]):
  var index = 0

  def peek: Token = tokens(index)

  def peekNext: Token = tokens(index + 1)

  final def peekValid(
    ignoreWhitespaces: Boolean = true,
    ignoreNewLines: Boolean = true
  ): Token = {
    var start = index
    while tokens(start).kind == TokenKind.Whitespace && ignoreWhitespaces ||
      tokens(start).kind == TokenKind.NewLine && ignoreNewLines do
      start += 1
    tokens(start)
  }

  @tailrec final def skipAndPeekValid(
    ignoreWhitespaces: Boolean = true,
    ignoreNewLines: Boolean = true
  ): Token = tokens(index) match {
    case Token(_, TokenKind.Whitespace, _) if ignoreWhitespaces =>
      index += 1
      skipAndPeekValid(ignoreWhitespaces, ignoreNewLines)
    case Token(_, TokenKind.NewLine, _) if ignoreNewLines =>
      index += 1
      skipAndPeekValid(ignoreWhitespaces, ignoreNewLines)
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
    val token = skipAndPeekValid(ignoreWhitespaces, ignoreNewLines)
    index += 1
    token

  def hasNext: Boolean = index < tokens.length
  
  def countIndent: Int =
    var indent = 0
    var pos = index
    while tokens(pos).kind == TokenKind.Whitespace do
      indent += tokens(pos).value.length
      pos += 1
    indent

  override def toString: String = tokens.drop(index).mkString(" ")
end TokenStream
