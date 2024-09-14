package me.gabriel.soma
package parser

import error.ParsingError.{InvalidBinaryOpError, NotImplementedFeatureError, UnexpectedTokenError}
import error.ParsingError
import parser.tree.{BinaryOperationNode, NumericNode, RootNode, SyntaxTree, SyntaxTreeNode}
import struct.{BinaryOp, Token, TokenKind, TokenStream}

class DefaultParser extends Parser {
    def parse(stream: TokenStream): Either[ParsingError, SyntaxTree] = {
        val bof = stream.next
        val expression = parseNumericExpression(stream)
        val root = expression.map(exp => RootNode(bof, List(exp)))
        root.map(SyntaxTree(_))
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
            val op = parseBinaryOpKind(stream.next.kind)
            val right = parseFactor(stream)
            left = for {
                l <- left
                op <- op
                r <- right
            } yield BinaryOperationNode(stream.peek, op, l, r)
        }
        left
    }
    
    private def parseFactor(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        val peek = stream.peek
        peek.kind match {
            case TokenKind.Number => parseNumberLiteral(stream)
            case _ => Left(UnexpectedTokenError(peek.value))
        }
    }
    
    private def parseNumberLiteral(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        val token = stream.next
        Right(NumericNode(token, List.empty))
    }

    private def parseBinaryOpKind(tokenKind: TokenKind): Either[ParsingError, BinaryOp] = tokenKind match {
        case TokenKind.Plus => Right(BinaryOp.Plus)
        case TokenKind.Minus => Right(BinaryOp.Minus)
        case TokenKind.Multiply => Right(BinaryOp.Multiply)
        case TokenKind.Divide => Right(BinaryOp.Divide)
        case _ => Left(InvalidBinaryOpError(tokenKind))
    }
}