package me.gabriel.seren
package parser

import error.ParsingError.{InvalidBinaryOpError, InvalidIdentifierError, UnexpectedTokenError, UnterminatedSequenceError}
import error.ParsingError
import parser.tree.*
import struct.{BinaryOp, Token, TokenKind, TokenStream}

class DefaultParser extends Parser {
    def parse(stream: TokenStream): Either[ParsingError, SyntaxTree] = {
        val bof = stream.next
        val topLevelDeclaration = parseTopLevelDeclaration(stream)
        val root = topLevelDeclaration.map(exp => RootNode(bof, List(exp)))
        root.map(SyntaxTree.apply)
    }

    private def parseTopLevelDeclaration(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        val peek = stream.peek
        peek.kind match {
            case TokenKind.Function => parseFunctionDeclaration(stream)
            case _ => Left(UnexpectedTokenError(peek))
        }
    }

    private def parseFunctionDeclaration(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        for {
            fnToken <- consumeToken(stream, TokenKind.Function)
            nameToken <- consumeToken(stream, TokenKind.Identifier)
            _ <- consumeToken(stream, TokenKind.LeftParenthesis)
            parameters <- parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, parseFunctionParameter)
            _ <- consumeToken(stream, TokenKind.LeftBrace)
            body <- parseSequence(stream, TokenKind.SemiColon, TokenKind.RightBrace, parseStatement)
        } yield FunctionDeclarationNode(fnToken, nameToken.value, parameters, body)
    }

    private def parseFunctionParameter(stream: TokenStream): Either[ParsingError, FunctionParameterNode] = {
        throw new NotImplementedError("Function parameters are not implemented yet")
    }

    private def parseExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        // TODO: implement equals check here
        parseNumericExpression(stream)
    }

    private def parseStatement(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        stream.peek.kind match {
            case TokenKind.Identifier => parseIdentifierStatement(stream)
            case _ => parseExpression(stream)
        }
    }

    private def parseIdentifierStatement(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        // TODO: assignments
        parseComplexExpression(stream)
    }

    private def parseComplexExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        // TODO: array access, nested calls, etc.
        parseIdentifierExpression(stream)
    }

    private def parseIdentifierExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        val token = consumeToken(stream, TokenKind.Identifier)
        if (token.isLeft) return Left(UnexpectedTokenError(stream.peek))

        val identifier = token.toOption.get

        stream.peek.kind match {
            case TokenKind.LeftParenthesis => parseFunctionCall(stream, identifier)
            case _ => Left(InvalidIdentifierError(stream.next))
        }
    }

    private def parseFunctionCall(stream: TokenStream, identifier: Token): Either[ParsingError, FunctionCallNode] = {
        for {
            _ <- consumeToken(stream, TokenKind.LeftParenthesis)
            arguments <- parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, parseExpression)
        } yield FunctionCallNode(identifier, identifier.value, arguments)
    }

    private def parseNumericExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        var left = parseTerm(stream)
        while (stream.peek.kind == TokenKind.Plus || stream.peek.kind == TokenKind.Minus) {
            val op = convertTokenKindToBinaryOp(stream.next.kind)
            val right = parseTerm(stream)
            left = for {
                l <- left
                r <- right
            } yield BinaryOperationNode(stream.peek, op, l, r)
        }
        left
    }
    
    private def parseTerm(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        var left = parseFactor(stream)
        while (stream.peek.kind == TokenKind.Multiply || stream.peek.kind == TokenKind.Divide) {
            val opToken = stream.next
            val op = convertTokenKindToBinaryOp(opToken.kind)
            val right = parseFactor(stream)
            left = for {
                l <- left
                r <- right
            } yield BinaryOperationNode(opToken, op, l, r)
        }
        left
    }
    
    private def parseFactor(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        val peek = stream.peek
        peek.kind match {
            case TokenKind.Number => parseNumber(stream)
            case TokenKind.String => parseString(stream)
            case _ => Left(UnexpectedTokenError(peek))
        }
    }

    private def parseNumber(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        parseNumberLiteral(stream).flatMap { n =>
            stream.peek.kind match {
                case TokenKind.Exponentiation =>
                    for {
                    opToken <- Right(stream.next)
                    right   <- parseNumberLiteral(stream)
                    } yield BinaryOperationNode(opToken, BinaryOp.Power, n, right)
                case _ => Right(n)
            }
        }
    }

    private def parseNumberLiteral(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        val token = stream.next
        Right(NumericNode(token))
    }

    private def parseString(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        Right(StringLiteralNode(stream.next))
    }

    private def parseSequence[T <: SyntaxTreeNode](
                               stream: TokenStream,
                               delimiter: TokenKind,
                               end: TokenKind,
                               parser: TokenStream => Either[ParsingError, T]
                             ): Either[ParsingError, List[T]] = {
        val nodes = collection.mutable.ListBuffer.empty[T]
        while (stream.peek.kind != end && stream.peek.kind != TokenKind.EOF) {
            val node = parser(stream)
            node match {
                case Left(error) => return Left(error)
                case Right(n) => nodes += n
            }
            val peek = stream.peek
            if (peek.kind == delimiter) {
                stream.next
            } else if (peek.kind != end) {
                return Left(UnterminatedSequenceError(peek))
            }
        }
        // fallback
        if (stream.peek.kind == TokenKind.EOF) {
            return Left(UnterminatedSequenceError(stream.peek))
        }
        stream.next
        Right(nodes.toList)
    }

    private def consumeToken(stream: TokenStream, kind: TokenKind): Either[ParsingError, Token] = {
        val token = stream.next
        if (token.kind == kind) {
            Right(token)
        } else {
            Left(UnexpectedTokenError(token))
        }
    }

    private def convertTokenKindToBinaryOp(tokenKind: TokenKind): BinaryOp = tokenKind match {
        case TokenKind.Plus => BinaryOp.Plus
        case TokenKind.Minus => BinaryOp.Minus
        case TokenKind.Multiply => BinaryOp.Multiply
        case TokenKind.Divide => BinaryOp.Divide
        case TokenKind.Exponentiation => BinaryOp.Power
        case _ => throw new IllegalArgumentException(s"Invalid binary operation: $tokenKind")
    }
}