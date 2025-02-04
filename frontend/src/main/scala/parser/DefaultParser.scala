package me.gabriel.seren.frontend
package parser

import error.ParsingError
import error.ParsingError.*
import parser.tree.*
import struct.*

class DefaultParser extends Parser {
  def parse(stream: TokenStream): Either[ParsingError, SyntaxTree] = {
    val bof = stream.next
    val topLevelDeclarations = parseFluidSequence(stream, TokenKind.EOF, parseTopLevelDeclaration)
    topLevelDeclarations match {
      case Left(error) => Left(error)
      case Right(declarations) => Right(SyntaxTree(RootNode(bof, declarations)))
    }
  }

  private def parseTopLevelDeclaration(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
    val peek = stream.skipAndPeekValid()
    peek.kind match {
      case TokenKind.Enum => parseEnum(stream)
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
      _ <- consumeToken(stream, TokenKind.LeftParenthesis)
      fields <- parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, parseStructField)
      functions <- consumeToken(stream, TokenKind.LeftBrace) match {
        case Left(_) => Right(List.empty)
        case Right(_) => parseFluidSequence(stream, TokenKind.RightBrace, s => parseFunctionDeclaration(s, Set.empty))
      }
    } yield StructDeclarationNode(structToken, nameToken.value, fields, functions)
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
  ): Either[ParsingError, FunctionDeclarationNode] = {
    val isC = modifiers.exists {
      case FunctionModifier.External(name) => name == "C"
      case _ => false
    }
    for {
      fnToken <- consumeToken(stream, TokenKind.Function)
      nameToken <- consumeToken(stream, TokenKind.Identifier)
      _ <- consumeToken(stream, TokenKind.LeftParenthesis)
      parameters <- parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, s => parseFunctionParameter(s, isC = isC))
      returnType <- consumeToken(stream, TokenKind.TypeDeclaration) match {
        case Right(_) => parseType(stream, isC = isC)
        case Left(_) => Right(Type.Void)
      }
      body <- if isC then
        Right(BlockNode(fnToken, List.empty))
      else
        parseBlock(stream)
    } yield FunctionDeclarationNode(fnToken, nameToken.value, Type.Function(parameters.map(_.nodeType), returnType), parameters, modifiers, body)
  }

  private def parseFunctionParameter(stream: TokenStream, isC: Boolean = false): Either[ParsingError, FunctionParameterNode] = {
    val isSelf = stream.peek.kind == TokenKind.This
    if (isSelf) {
      return Right(FunctionParameterNode(stream.next, "this", Type.UnknownThis))
    }
    for {
      nameToken <- if isC then
        Right(Token("", TokenKind.Identifier, -2))
      else
        consumeToken(stream, TokenKind.Identifier)
      _ <- if isC then
        Right(())
      else
        consumeToken(stream, TokenKind.TypeDeclaration)
      parameterType <- parseType(stream, isC = isC)
    } yield FunctionParameterNode(nameToken, nameToken.value, parameterType)
  }

  private def parseExpression(stream: TokenStream): Either[ParsingError, TypedSyntaxTreeNode] = {
    val leftExpr = parseNumericExpression(stream)
    val current = stream.peekValid()
    if (current.kind == TokenKind.Equal) {
      val assignment = for {
        left <- leftExpr
        _ <- consumeToken(stream, TokenKind.Equal)
        rightExpr <- parseExpression(stream)
      } yield EqualityNode(current, left, rightExpr)
      assignment
    } else {
      leftExpr
    }
  }

  private def parseStatement(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
    stream.skipAndPeekValid().kind match {
      case TokenKind.Identifier => parseIdentifierStatement(stream)
      case TokenKind.If => parseIfStatement(stream)
      case TokenKind.Return => parseReturnStatement(stream)
      case _ => parseExpression(stream)
    }
  }
  
  private def parseIdentifierStatement(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
    if (stream.peekNextValid().kind == TokenKind.Assign) {
      return parseAssignment(stream)
    }

    parseComplexExpression(stream)
  }

  private def parseComplexExpression(stream: TokenStream): Either[ParsingError, SyntaxTreeNode] = {
    // TODO: array access, nested calls, etc.
    parseIdentifierExpression(stream)
  }

  private def parseIdentifierExpression(stream: TokenStream): Either[ParsingError, TypedSyntaxTreeNode] = {
    val token = consumeOneOfTokens(stream, Array(TokenKind.Identifier, TokenKind.This))
    if (token.isLeft) return Left(UnexpectedTokenError(stream.peek))

    val identifier = token.toOption.get

    stream.peek.kind match {
      case TokenKind.LeftParenthesis => parseFunctionCall(stream, identifier)
      case _ => parseIdentifierAccess(
        stream,
        ReferenceNode(identifier, identifier.value, identifier.kind match {
          case TokenKind.Identifier => Type.Unknown
          case TokenKind.This => Type.UnknownThis
          case _ => return Left(UnexpectedTokenError(identifier))
        })
      )
    }
  }

  private def parseIfStatement(stream: TokenStream): Either[ParsingError, IfNode] = {
    for {
      ifToken <- consumeToken(stream, TokenKind.If)
      condition <- parseExpression(stream)
      thenBlock <- parseBlock(stream)
      elseBlock <- if (stream.peekValid().kind == TokenKind.Else) {
        stream.nextValid()
        parseBlock(stream).map(Some(_))
      } else {
        Right(None)
      }
    } yield IfNode(ifToken, condition, thenBlock, elseBlock)
  }

  private def parseIdentifierAccess(stream: TokenStream, reference: ReferenceNode): Either[ParsingError, TypedSyntaxTreeNode] = {
    var currentNode: TypedSyntaxTreeNode = reference
    while (true) {
      stream.peek.kind match {
        case TokenKind.Dot =>
          val structAccess = for {
            accessToken <- consumeToken(stream, TokenKind.Dot)
            identifier <- consumeToken(stream, TokenKind.Identifier)
          } yield StructFieldAccessNode(
            token = accessToken,
            struct = currentNode,
            fieldName = identifier.value,
            nodeType = Type.Unknown
          )
          if (structAccess.isLeft) return structAccess
          currentNode = structAccess.toOption.get
        case _ => return Right(currentNode)
      }
    }
    Right(currentNode)
  }

  private def parseReturnStatement(stream: TokenStream): Either[ParsingError, ReturnNode] = {
    println("Parsing return statement")
    for {
      returnToken <- consumeToken(stream, TokenKind.Return)
      expression = parseExpression(stream).toOption
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
    while (stream.peek.kind == TokenKind.Asterisk || stream.peek.kind == TokenKind.Divide) {
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
    val peek = stream.skipAndPeekValid()
    val value = peek.kind match {
      case TokenKind.Null =>
        stream.next
        Right(NullNode(peek))
      case TokenKind.NumberLiteral(_) => parseNumberLiteral(stream)
      case TokenKind.StringLiteral => parseString(stream)
      case TokenKind.Modulo => parseInstantiation(stream)
      case TokenKind.Identifier | TokenKind.This => parseIdentifierExpression(stream)
      case TokenKind.LeftParenthesis =>
        stream.next
        val expression = parseExpression(stream)
        consumeToken(stream, TokenKind.RightParenthesis)
        expression
      case _ => Left(UnexpectedTokenError(peek))
    }
    (stream.peekValid().kind, value) match {
      case (TokenKind.As, Right(value)) => parseCast(stream, value)
      case _ => value
    }
  }

  private def parseNumberLiteral(stream: TokenStream): Either[ParsingError, NumericNode] = {
    val token = stream.next
    Right(NumericNode(token, token.kind.asInstanceOf[TokenKind.NumberLiteral].suffixType.getOrElse(Type.Int)))
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

  private def parseInstantiation(stream: TokenStream): Either[ParsingError, StructInstantiationNode] = {
    for {
      instantiationToken <- consumeToken(stream, TokenKind.Modulo)
      structNameToken <- consumeToken(stream, TokenKind.Identifier)
      _ <- consumeToken(stream, TokenKind.LeftParenthesis)
      arguments <- parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, parseExpression)
    } yield StructInstantiationNode(instantiationToken, structNameToken.value, Type.UnknownIdentifier(structNameToken.value), arguments)
  }
  
  private def parseCast(stream: TokenStream, expression: TypedSyntaxTreeNode): Either[ParsingError, CastNode] = {
    for {
      castToken <- consumeToken(stream, TokenKind.As)
      targetType <- parseType(stream)
    } yield CastNode(castToken, expression, targetType)
  }

  private def parseEnum(stream: TokenStream): Either[ParsingError, EnumDeclarationNode] = {
    for {
      enumToken <- consumeToken(stream, TokenKind.Enum)
      nameToken <- consumeToken(stream, TokenKind.Identifier)
      _ <- consumeToken(stream, TokenKind.Colon)
      variants <- parseExhaustiveSequence(stream, TokenKind.Pipe, parseEnumVariant)
    } yield EnumDeclarationNode(enumToken, nameToken.value, variants)
  }

  private def parseEnumVariant(stream: TokenStream): Either[ParsingError, EnumVariantNode] = {
    for {
      nameToken <- consumeToken(stream, TokenKind.Identifier)
      types <- consumeToken(stream, TokenKind.LeftParenthesis) match {
        case Left(_) => Right(List.empty)
        case Right(_) => parseSequence(stream, TokenKind.Comma, TokenKind.RightParenthesis, s => parseType(s))
      }
    } yield EnumVariantNode(nameToken, nameToken.value, types)
  }

  private def parseBlock(stream: TokenStream): Either[ParsingError, BlockNode] = {
    val openingBrace = consumeToken(stream, TokenKind.LeftBrace)
    val instructions = parseSequence(
      stream,
      TokenKind.NewLine,
      TokenKind.RightBrace,
      parseStatement,
      ignoreNewLines = false
    )
    for {
      brace <- openingBrace
      ins <- instructions
    } yield BlockNode(brace, ins)
  }

  private def parseFluidSequence[T <: SyntaxTreeNode](
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

  private def parseExhaustiveSequence[T](
    stream: TokenStream,
    delimiter: TokenKind,
    parser: TokenStream => Either[ParsingError, T],
    ignoreWhitespace: Boolean = true,
    ignoreNewLines: Boolean = true
  ): Either[ParsingError, List[T]] = {
    val nodes = collection.mutable.ListBuffer.empty[T]
    while (stream.peek.kind != TokenKind.EOF) {
      val node = parser(stream)
      node match
        case Left(error) => return Left(error)
        case Right(n) => nodes += n
      val peek = stream.peekValid(
        ignoreWhitespaces = ignoreWhitespace,
        ignoreNewLines = ignoreNewLines
      )
      peek.kind match
        case del if del == delimiter => stream.nextValid(
          ignoreWhitespaces = ignoreWhitespace,
          ignoreNewLines = ignoreNewLines
        )
        case TokenKind.EOF | _ => return Right(nodes.toList)
    }
    Right(nodes.toList)
  }

  private def parseSequence[T](
    stream: TokenStream,
    delimiter: TokenKind,
    end: TokenKind,
    parser: TokenStream => Either[ParsingError, T],
    ignoreWhitespace: Boolean = true,
    ignoreNewLines: Boolean = true
  ): Either[ParsingError, List[T]] = {
    val nodes = collection.mutable.ListBuffer.empty[T]
    while (stream.peek.kind != end) {
      val node = parser(stream)
      node match
        case Left(error) => return Left(error)
        case Right(n) => nodes += n
      val peek = stream.peekValid(
        ignoreWhitespaces = ignoreWhitespace,
        ignoreNewLines = ignoreNewLines
      )

      val trailingPreventivePeek = stream.peekValid()
      if trailingPreventivePeek.kind == end then
        stream.nextValid()
        return Right(nodes.toList)
      peek.kind match
        case del if del == delimiter => stream.nextValid(
          ignoreWhitespaces = ignoreWhitespace,
          ignoreNewLines = ignoreNewLines
        )
        case ending if ending == end =>
          val before = stream.peekValid()
          stream.nextValid()
          return Right(nodes.toList)
        case TokenKind.EOF | _ => return Left(UnterminatedSequenceError(peek))
    }
    if (stream.peek.kind == TokenKind.EOF) {
      return Left(UnterminatedSequenceError(stream.peek))
    }
    stream.nextValid(
      ignoreWhitespaces = ignoreWhitespace,
      ignoreNewLines = ignoreNewLines
    )
    Right(nodes.toList)
  }

  private def consumeToken(
    stream: TokenStream,
    kind: TokenKind,
    ignoreWhitespace: Boolean = true
  ): Either[ParsingError, Token] = {
    val token = stream.skipAndPeekValid(ignoreWhitespaces = ignoreWhitespace)
    if (token.kind == kind) {
      stream.next
      Right(token)
    } else {
      Left(ExpectedDifferentTokenError(token, kind))
    }
  }

  private def consumeOneOfTokens(
    stream: TokenStream,
    tokens: Array[TokenKind],
    ignoreWhitespace: Boolean = true
  ): Either[ParsingError, Token] = {
    val token = stream.skipAndPeekValid(ignoreWhitespaces = ignoreWhitespace)
    if (tokens.contains(token.kind)) {
      stream.next
      Right(token)
    } else {
      Left(ExpectedDifferentTokenError(token, tokens.head))
    }
  }

  private def parseType(stream: TokenStream, isC: Boolean = false): Either[ParsingError, Type] = {
    val token = stream.nextValid()
    val base = token.kind match {
      case _ if isC => Right(Type.CType(token.value))
      case TokenKind.VoidType => Right(Type.Void)
      case TokenKind.Int8Type => Right(Type.Byte)
      case TokenKind.Int16Type => Right(Type.Short)
      case TokenKind.Int32Type => Right(Type.Int)
      case TokenKind.Int64Type => Right(Type.Long)
      case TokenKind.UsizeType => Right(Type.Usize)
      case TokenKind.StringType => Right(Type.String)
      case TokenKind.This => Right(Type.UnknownThis)
      case TokenKind.AnyType => Right(Type.Any)
      case TokenKind.Identifier => Right(Type.UnknownIdentifier(token.value))
      case _ => Left(InvalidTypeDeclarationError(token))
    }
    base -> stream.peek.kind match
      case (Right(base), TokenKind.Vararg) =>
        stream.next
        Right(Type.Vararg(base))
      case (Right(base), TokenKind.Asterisk) =>
        stream.next
        Right(Type.Pointer(base))
      case _ => base
  }

  private def convertTokenKindToBinaryOp(tokenKind: TokenKind): BinaryOp = tokenKind match {
    case TokenKind.Plus => BinaryOp.Plus
    case TokenKind.Minus => BinaryOp.Minus
    case TokenKind.Asterisk => BinaryOp.Multiply
    case TokenKind.Divide => BinaryOp.Divide
    case _ => throw new IllegalArgumentException(s"Invalid binary operation: $tokenKind")
  }
}