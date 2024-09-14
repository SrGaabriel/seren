package me.gabriel.soma
package struct

class TokenStream(tokens: List[Token]):
  private var index = 0

  def peek: Token = tokens(index)
  def peekNext: Token = tokens(index + 1)

  def next: Token =
    val token = peek
    index += 1
    token

  def hasNext: Boolean = index < tokens.length
end TokenStream
