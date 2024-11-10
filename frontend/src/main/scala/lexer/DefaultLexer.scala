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
        case ' ' | '\t' | '\r' => position += 1
        case '\n' => addToken("\n", TokenKind.NewLine)
        case '+' => addToken("+", TokenKind.Plus)
        case '-' => addToken("-", TokenKind.Minus)
        case '*' => addToken("*", TokenKind.Multiply)
        case '/' => addToken("/", TokenKind.Divide)
        case '(' => addToken("(", TokenKind.LeftParenthesis)
        case ')' => addToken(")", TokenKind.RightParenthesis)
        case '^' => addToken("^", TokenKind.Exponentiation)
        case '{' => addToken("{", TokenKind.LeftBrace)
        case '}' => addToken("}", TokenKind.RightBrace)
        case '<' => addToken("<", TokenKind.LeftAngleBracket)
        case '>' => addToken(">", TokenKind.RightAngleBracket)
//        case ';' => addToken(";", TokenKind.SemiColon)
        case ':' =>
          if (input(position + 1) == '=') {
            addToken(":=", TokenKind.Assign)
          } else {
            addToken(":", TokenKind.TypeDeclaration)
          }
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
            case "struct" => TokenKind.Struct
            case "string" => TokenKind.StringLiteral
            case "external" => TokenKind.External
            case _ => TokenKind.Identifier
          }
          addToken(identifier, tokenKind)
        case _ => return Left(LexicalError.UnexpectedCharacterError(currentChar, position))
      }
    }

    tokens += Token("", TokenKind.EOF)
    Right(optimizeTokenStream(tokens.toList))
  }

  def optimizeTokenStream(tokens: List[Token]): List[Token] = {
    val result = scala.collection.mutable.ListBuffer[Token]()
    var i = 0

    while (i < tokens.length) {
      val current = tokens(i)

      if (current.kind == TokenKind.NewLine) {
        var j = i + 1
        // Skip consecutive newlines
        while (j < tokens.length && tokens(j).kind == TokenKind.NewLine) {
          j += 1
        }

        // Check if we should keep this newline
        if (i > 0 && j < tokens.length &&
          !DefaultLexer.precedingExclusions.contains(tokens(i - 1).kind) &&
          !DefaultLexer.followingExclusions.contains(tokens(j).kind)) {
          result += current
        }

        i = j // Move to the token after all consecutive newlines
      } else {
        result += current
        i += 1
      }
    }

    result.toList
  }

  object DefaultLexer {
    val precedingExclusions: Set[TokenKind] = Set(
      TokenKind.LeftParenthesis,
      TokenKind.LeftBrace,
      TokenKind.Comma,
      TokenKind.Plus,
      TokenKind.Minus,
      TokenKind.Multiply,
      TokenKind.Divide,
      TokenKind.Exponentiation,
      TokenKind.Assign,
      TokenKind.Let,
      TokenKind.Function,
      TokenKind.Return,
      TokenKind.BOF,
      TokenKind.RightParenthesis,
      TokenKind.RightBrace,
    )

    val followingExclusions: Set[TokenKind] = Set(
      TokenKind.RightParenthesis,
      TokenKind.RightBrace,
      TokenKind.Comma,
      TokenKind.Plus,
      TokenKind.Minus,
      TokenKind.Multiply,
      TokenKind.Divide,
      TokenKind.Exponentiation,
      TokenKind.SemiColon,
      TokenKind.Function,
      TokenKind.EOF,
    )
  }
}