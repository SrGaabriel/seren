package me.gabriel.seren.frontend
package parser

import error.ParsingError.*
import error.ParsingError
import parser.tree.*
import struct.{BinaryOp, FunctionModifier, Token, TokenKind, TokenStream}

class DefaultParser extends Parser {
    def parse(stream: TokenStream): Either[ParsingError, SyntaxTree] = {
        val bof = stream.next
        val topLevelDeclarations = parseExhaustiveSequence(stream, TokenKind.EOF, parseTopLevelDeclaration)
        topLevelDeclarations match {
            case Left(error) => Left(error)
            case Right(declarations) => Right(SyntaxTree(RootNode(bof, declarations)))
        }
    }

    private def parseTopLevelDeclaration(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        val peek = stream.peek
        peek.kind match {
            // todo: improve this
            case TokenKind.External =>
                for {
                    externalToken <- consumeToken(stream, TokenKind.External)
                    leftAngleBracket <- consumeToken(stream, TokenKind.LeftAngleBracket)
                    nameToken <- consumeToken(stream, TokenKind.Identifier)
                    _ <- consumeToken(stream, TokenKind.RightAngleBracket)
                    declaration <- parseFunctionDeclaration(stream, Set(FunctionModifier.External(nameToken.value)))
                } yield declaration
            case TokenKind.Function => parseFunctionDeclaration(stream, Set.empty)
            case TokenKind.Struct => parseStructDeclaration(stream)
            case _ => Left(UnexpectedTokenError(peek))
        }
    }

    private def parseStructDeclaration(stream: TokenStream): Either[ParsingError, StructDeclarationNode] = {
        for {
            structToken <- consumeToken(stream, TokenKind.Struct)
            nameToken <- consumeToken(stream, TokenKind.Identifier)
            _ <- consumeToken(stream, TokenKind.LeftBrace)
            fields <- parseSequence(stream, TokenKind.NewLine, TokenKind.RightBrace, parseStructField)
        } yield StructDeclarationNode(structToken, nameToken.value, fields)
    }

    private def parseStructField(stream: TokenStream): Either[ParsingError, StructFieldNode] = {
        for {
            nameToken <- consumeToken(stream, TokenKind.Identifier)
            _ <- consumeToken(stream, TokenKind.TypeDeclaration)
            fieldType <- parseType(stream)
        } yield StructFieldNode(nameToken, nameToken.value, fieldType)
    }

    private def parseFunctionDeclaration(
                                          stream: TokenStream,
                                          modifiers: Set[FunctionModifier]
                                        ): Either[ParsingError, SyntaxTreeNode] = {
        val isC = modifiers.exists {
            case FunctionModifier.External(name) => name == "C"
            case _ => false
        }
        for {
            fnToken <- consumeToken(stream, TokenKind.Function)
            nameToken <- consumeToken(stream, TokenKind.Identifier)
            _ <- consumeToken(stream, TokenKind.LeftParenthesis)
            parameters <- parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, s => parseFunctionParameter(s, isC=isC))
            returnType <- consumeToken(stream, TokenKind.TypeDeclaration) match {
                case Right(_) => parseType(stream, isC = isC)
                case Left(_) => Right(Type.Void)
            }
            body <- parseBlock(stream)
        } yield FunctionDeclarationNode(fnToken, nameToken.value, returnType, parameters, modifiers, body)
    }

    private def parseFunctionParameter(stream: TokenStream, isC: Boolean=false): Either[ParsingError, FunctionParameterNode] = {
        for {
            nameToken <- consumeToken(stream, TokenKind.Identifier)
            _ <- consumeToken(stream, TokenKind.TypeDeclaration)
            parameterType <- parseType(stream, isC=isC)
        } yield FunctionParameterNode(nameToken, nameToken.value, parameterType)
    }

    private def parseExpression(stream: TokenStream): Either[ParsingError, TypedSyntaxTreeNode] = {
        // TODO: implement equals check here
        parseNumericExpression(stream)
    }

    private def parseStatement(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        stream.peek.kind match {
            case TokenKind.Identifier => parseIdentifierStatement(stream)
            case TokenKind.Return => parseReturnStatement(stream)
            case _ => parseExpression(stream)
        }
    }

    private def parseIdentifierStatement(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        if (stream.peekNext.kind == TokenKind.Assign) {
            return parseAssignment(stream)
        }

        parseComplexExpression(stream)
    }

    private def parseComplexExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
        // TODO: array access, nested calls, etc.
        parseIdentifierExpression(stream)
    }

    private def parseIdentifierExpression(stream: TokenStream): Either[ParsingError, TypedSyntaxTreeNode] = {
        val token = consumeToken(stream, TokenKind.Identifier)
        if (token.isLeft) return Left(UnexpectedTokenError(stream.peek))

        val identifier = token.toOption.get

        stream.peek.kind match {
            case TokenKind.LeftParenthesis => parseFunctionCall(stream, identifier)
            case _ => Right(ReferenceNode(identifier, identifier.value, Type.Unknown))
        }
    }

    private def parseReturnStatement(stream: TokenStream): Either[ParsingError, ReturnNode] = {
        for {
            returnToken <- consumeToken(stream, TokenKind.Return)
            expression <- parseExpression(stream)
        } yield ReturnNode(returnToken, expression)
    }

    private def parseFunctionCall(stream: TokenStream, identifier: Token): Either[ParsingError, FunctionCallNode] = {
        for {
            _ <- consumeToken(stream, TokenKind.LeftParenthesis)
            arguments <- parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, parseExpression)
        } yield FunctionCallNode(identifier, identifier.value, arguments)
    }

    private def parseNumericExpression(stream: TokenStream): Either[ParsingError, TypedSyntaxTreeNode] = {
        var left = parseTerm(stream)
        while (stream.peek.kind == TokenKind.Plus || stream.peek.kind == TokenKind.Minus) {
            val op = convertTokenKindToBinaryOp(stream.next.kind)
            val right = parseTerm(stream)
            left = for {
                l <- left
                r <- right
            } yield BinaryOperationNode(
                stream.peek,
                op,
                l,
                r
            )
        }
        left
    }
    
    private def parseTerm(stream: TokenStream): Either[ParsingError, TypedSyntaxTreeNode] = {
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
    
    private def parseFactor(stream: TokenStream): Either[ParsingError, TypedSyntaxTreeNode] = {
        val peek = stream.peek
        peek.kind match {
            case TokenKind.NumberLiteral => parseNumberLiteral(stream)
            case TokenKind.StringLiteral => parseString(stream)
            case TokenKind.Identifier => parseIdentifierExpression(stream)
            case _ => Left(UnexpectedTokenError(peek))
        }
    }

    private def parseNumberLiteral(stream: TokenStream): Either[ParsingError, NumericNode] = {
        val token = stream.next
        Right(NumericNode(token, Type.Int))
    }

    private def parseString(stream: TokenStream): Either[ParsingError, StringLiteralNode] = {
        Right(StringLiteralNode(stream.next))
    }

    private def parseAssignment(stream: TokenStream): Either[ParsingError, AssignmentNode] = {
        for {
            identifierToken <- consumeToken(stream, TokenKind.Identifier)
            _ <- consumeToken(stream, TokenKind.Assign)
            expression <- parseExpression(stream)
        } yield AssignmentNode(identifierToken, identifierToken.value, expression)
    }
    
    private def parseBlock(stream: TokenStream): Either[ParsingError, BlockNode] = {
        val openingBrace = consumeToken(stream, TokenKind.LeftBrace)
        val instructions = parseSequence(
            stream,
            TokenKind.NewLine, 
            TokenKind.RightBrace, 
            parseStatement
        )
        for {
            brace <- openingBrace
            ins <- instructions
        } yield BlockNode(brace, ins)
    }

    private def parseExhaustiveSequence[T <: SyntaxTreeNode](
                               stream: TokenStream,
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
        }
        consumeToken(stream, end).map(_ => nodes.toList)
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
        val token = stream.peek
        if (token.kind == kind) {
            stream.next
            Right(token)
        } else {
            Left(ExpectedTokenError(token, kind))
        }
    }

    private def parseType(stream: TokenStream, isC: Boolean=false): Either[ParsingError, Type] = {
        val token = stream.next
        if (isC) {
            stream.peek.kind match {
                case TokenKind.Multiply =>
                    stream.next
                    return Right(Type.CType(token.value + "*"))
                case _ => return Right(Type.CType(token.value))
            }
        }
        val base = token.kind match {
            case TokenKind.VoidType => Right(Type.Void)
            case TokenKind.Int32Type => Right(Type.Int)
            case TokenKind.StringLiteral => Right(Type.String)
            case TokenKind.AnyType => Right(Type.Any)
            case _ => Left(InvalidTypeDeclarationError(token))
        }
        if (stream.peek.kind == TokenKind.Vararg) {
            stream.next
            Right(Type.Vararg(base.toOption.get))
        } else {
            base
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