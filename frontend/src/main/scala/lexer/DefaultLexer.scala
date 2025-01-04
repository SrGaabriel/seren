package me.gabriel.seren.frontend
package lexer

import error.LexicalError
import parser.Type
import struct.{Token, TokenKind}

class DefaultLexer extends Lexer {
  def lex(input: String): Either[LexicalError, List[Token]] = {
    val tokens = collection.mutable.ListBuffer(Token("", TokenKind.BOF, -1))
    var position = 0

    def addToken(value: String, kind: TokenKind): Unit = {
      tokens += Token(value, kind, position)
      position += value.length
    }

    while (position < input.length) {
      val currentChar = input(position)

      currentChar match {
        case ' ' | '\t' =>
          val whitespace = input.drop(position).takeWhile(c => c == ' ' || c == '\t')
          addToken(whitespace, TokenKind.Whitespace)
        case '\r' =>
          if (position + 1 < input.length && input(position + 1) == '\n') {
            addToken("\r\n", TokenKind.NewLine)
          } else {
            addToken("\r", TokenKind.NewLine)
          }
        case '\n' =>
          addToken("\n", TokenKind.NewLine)
        case '+' => addToken("+", TokenKind.Plus)
        case '-' => addToken("-", TokenKind.Minus)
        case '*' => addToken("*", TokenKind.Multiply)
        case '(' => addToken("(", TokenKind.LeftParenthesis)
        case ')' => addToken(")", TokenKind.RightParenthesis)
        case '{' => addToken("{", TokenKind.LeftBrace)
        case '}' => addToken("}", TokenKind.RightBrace)
        case '<' => addToken("<", TokenKind.LeftAngleBracket)
        case '>' => addToken(">", TokenKind.RightAngleBracket)
        case ',' => addToken(",", TokenKind.Comma)
        case '|' => addToken("|", TokenKind.Pipe)
        case '%' => addToken("%", TokenKind.Modulo)
        //        case ';' => addToken(";", TokenKind.SemiColon)
        case ':' =>
          input(position + 1) match {
            case ':' =>
              addToken("::", TokenKind.TypeDeclaration)
            case '=' =>
              addToken(":=", TokenKind.Assign)
            case _ =>
              addToken(":", TokenKind.Colon)
          }
        case '.' if input.drop(position).startsWith("...") =>
          addToken("...", TokenKind.Vararg)
        case '.' => addToken(".", TokenKind.Dot)
        case '/' if input.drop(position).startsWith("//") =>
          val comment = input.drop(position).takeWhile(_ != '\n')
          position += comment.length
        case '/' => addToken("/", TokenKind.Divide)
        case '"' =>
          val string = input.drop(position + 1).takeWhile(_ != '"')
          if (string.isEmpty) return Left(LexicalError.UnterminatedString(position))
          addToken(string, TokenKind.StringLiteral)
          position += 2
        case _ if currentChar.isDigit =>
          val number = input.drop(position).takeWhile(_.isDigit)
          val (numericType, extraPositions) = input.drop(position + number.length) match {
            case e if e.startsWith("i64") => (Some(Type.Long), 3)
            case e if e.startsWith("i32") => (Some(Type.Int), 3)
            case e if e.startsWith("i16") => (Some(Type.Short), 3)
            case e if e.startsWith("i8") => (Some(Type.Byte), 2)
            case _ => (None, 0)
          }

          addToken(number, TokenKind.NumberLiteral(numericType))
          position += extraPositions
        case _ if currentChar.isLetter || currentChar == '_' =>
          val identifier = input.drop(position).takeWhile(c => c.isLetterOrDigit || c == '_')
          val tokenKind = identifier match {
            case "let" => TokenKind.Let
            case "fun" => TokenKind.Function
            case "ret" => TokenKind.Return
            case "any" => TokenKind.AnyType
            case "this" => TokenKind.This
            case "void" => TokenKind.VoidType
            case "int8" => TokenKind.Int8Type
            case "int16" => TokenKind.Int16Type
            case "int32" => TokenKind.Int32Type
            case "int64" => TokenKind.Int64Type
            case "struct" => TokenKind.Struct
            case "string" => TokenKind.StringType
            case "enum" => TokenKind.Enum
            case "external" => TokenKind.External
            case _ => TokenKind.Identifier
          }
          addToken(identifier, tokenKind)
        case _ => return Left(LexicalError.UnexpectedCharacterError(currentChar, position))
      }
    }

    tokens += Token("", TokenKind.EOF, position)
    Right(tokens.toList)
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
      TokenKind.Assign,
      TokenKind.Let,
      TokenKind.Function,
      TokenKind.Return,
      TokenKind.BOF,
      TokenKind.RightBrace,
      TokenKind.Pipe,
    )

    val followingExclusions: Set[TokenKind] = Set(
      TokenKind.RightParenthesis,
      TokenKind.RightBrace,
      TokenKind.Comma,
      TokenKind.Plus,
      TokenKind.Minus,
      TokenKind.Multiply,
      TokenKind.Divide,
      TokenKind.SemiColon,
      TokenKind.Function,
      TokenKind.EOF,
    )
  }
}