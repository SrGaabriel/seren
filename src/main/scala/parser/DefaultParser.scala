package me.gabriel.seren
package parser

import error.ParsingError.{InvalidBinaryOpError, UnexpectedTokenError}
import error.ParsingError
import parser.tree.*
import struct.{BinaryOp, Token, TokenKind, TokenStream}

class DefaultParser extends Parser {
    def parse(stream: TokenStream): Either[ParsingError, SyntaxTree] = {
        val bof = stream.next
        val expression = parseExpression(stream)
        val root = expression.map(exp => RootNode(bof, List(exp)))
        root.map(SyntaxTree(_))
    }

    private def parseExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        // TODO: implement equals check here
        parseNumericExpression(stream)
    }

    private def parseNumericExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        var left = parseTerm(stream)
        while (stream.peek.kind == TokenKind.Plus || stream.peek.kind == TokenKind.Minus) {
            val op = parseBinaryOpKind(stream.next.kind)
            val right = parseTerm(stream)
            left = for {
                l <- left
                op <- op
                r <- right
            } yield BinaryOperationNode(stream.peek, op, l, r)
        }
        left
    }
    
    private def parseTerm(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        var left = parseFactor(stream)
        while (stream.peek.kind == TokenKind.Multiply || stream.peek.kind == TokenKind.Divide) {
            val opToken = stream.next
            val op = parseBinaryOpKind(opToken.kind)
            val right = parseFactor(stream)
            left = for {
                l <- left
                op <- op
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

    private def parseBinaryOpKind(tokenKind: TokenKind): Either[ParsingError, BinaryOp] = tokenKind match {
        case TokenKind.Plus => Right(BinaryOp.Plus)
        case TokenKind.Minus => Right(BinaryOp.Minus)
        case TokenKind.Multiply => Right(BinaryOp.Multiply)
        case TokenKind.Divide => Right(BinaryOp.Divide)
        case TokenKind.Exponentiation => Right(BinaryOp.Power)
    }
}