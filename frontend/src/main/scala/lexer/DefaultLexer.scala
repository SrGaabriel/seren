package me.gabriel.seren.frontend
package lexer

import error.LexicalError
import struct.{Token, TokenKind}

class DefaultLexer extends Lexer {
  def lex(input: String): Either[LexicalError, List[Token]] = {
    val tokens = collection.mutable.ListBuffer(Token("", TokenKind.BOF))
    var position = 0

    def addToken(value: String, kind: TokenKind): Unit = {
      tokens += Token(value, kind)
      position += value.length
    }

    while (position < input.length) {
      val currentChar = input(position)

      currentChar match {
        case ' ' | '\t' | '\n' | '\r' => position += 1
        case '+' => addToken("+", TokenKind.Plus)
        case '-' => addToken("-", TokenKind.Minus)
        case '*' => addToken("*", TokenKind.Multiply)
        case '/' => addToken("/", TokenKind.Divide)
        case '(' => addToken("(", TokenKind.LeftParenthesis)
        case ')' => addToken(")", TokenKind.RightParenthesis)
        case '^' => addToken("^", TokenKind.Exponentiation)
        case '{' => addToken("{", TokenKind.LeftBrace)
        case '}' => addToken("}", TokenKind.RightBrace)
        case ';' => addToken(";", TokenKind.SemiColon)
        case ':' => addToken(":", TokenKind.TypeDeclaration)
        case '"' =>
          val string = input.drop(position + 1).takeWhile(_ != '"')
          if (string.isEmpty) return Left(LexicalError.UnterminatedString(position))
          addToken(string, TokenKind.StringLiteral)
          position += 2 // For the quotes
        case _ if currentChar.isDigit =>
          val number = input.drop(position).takeWhile(_.isDigit)
          addToken(number, TokenKind.NumberLiteral)
        case _ if currentChar.isLetter || currentChar == '_' =>
          val identifier = input.drop(position).takeWhile(c => c.isLetterOrDigit || c == '_')
          val tokenKind = identifier match {
            case "let" => TokenKind.Let
            case "fn" => TokenKind.Function
            case "ret" => TokenKind.Return
            case "void" => TokenKind.VoidType
            case "int32" => TokenKind.Int32Type
            case "string" => TokenKind.StringLiteral
            case _ => TokenKind.Identifier
          }
          addToken(identifier, tokenKind)
        case _ => return Left(LexicalError.UnexpectedCharacterError(currentChar, position))
      }
    }

    tokens += Token("", TokenKind.EOF)
    Right(tokens.toList)
  }
}
