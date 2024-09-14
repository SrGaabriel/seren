package me.gabriel.soma
package lexer

import error.{LexicalError, UnexpectedCharacterError}
import struct.{Token, TokenKind}

class DefaultLexer extends Lexer {
  def lex(input: String): Either[LexicalError, List[Token]] = {
    val tokens = collection.mutable.ListBuffer(Token("", TokenKind.BOF))
    var position = 0

    while (position < input.length) {
      val currentChar = input(position)

      currentChar match {
        case ' ' | '\t' | '\n' => {
          position += 1
        }
        case '+' => {
          tokens += Token("+", TokenKind.Plus)
          position += 1
        }
        case '-' => {
          tokens += Token("-", TokenKind.Minus)
          position += 1
        }
        case '*' => {
          tokens += Token("*", TokenKind.Multiply)
          position += 1
        }
        case '/' => {
          tokens += Token("/", TokenKind.Divide)
          position += 1
        }
        case '(' => {
          tokens += Token("(", TokenKind.LeftParenthesis)
          position += 1
        }
        case ')' => {
          tokens += Token(")", TokenKind.RightParenthesis)
          position += 1
        }
        case _ if currentChar.isDigit => {
          val number = currentChar.toString + input.drop(position + 1).takeWhile(_.isDigit)
          tokens += Token(number, TokenKind.Number)
          position += number.length
        }
        case _ => {
          return Left(UnexpectedCharacterError(currentChar))
        }
      }
    }

    tokens += Token("", TokenKind.EOF)
    Right(tokens.toList)
  }
}
