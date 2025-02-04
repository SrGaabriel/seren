package me.gabriel.seren.llvm
package session

import target.CompilationTarget
import util.IterableExtensions.IterableLCM
import util.{PaddingSorter, convertControlChars}

import me.gabriel.seren.analyzer.{SymbolBlock, TypeEnvironment}
import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.*
import me.gabriel.seren.frontend.struct.FunctionModifier.External
import me.gabriel.seren.frontend.struct.{BinaryOp, FunctionModifier}
import me.gabriel.seren.logging.LogLevel
import me.gabriel.seren.logging.tracing.Traceable
import me.gabriel.tianlong.TianlongModule
import me.gabriel.tianlong.factory.StatementHolder
import me.gabriel.tianlong.statement.*
import me.gabriel.tianlong.struct.*
import me.gabriel.tianlong.struct.DragonType.Int32

import scala.collection.mutable

class TianlongCompilerSession(
  val syntaxTree: SyntaxTree,
  val typeEnvironment: TypeEnvironment,
  val target: CompilationTarget
) extends Traceable {
  val module = TianlongModule()
  val memoryReferences: mutable.Map[SymbolBlock, mutable.HashMap[String, MemoryReference]] = mutable.HashMap()

  def finish(): TianlongModule = module

  def generateTree(): Unit = {
    syntaxTree.root.children.foreach(generateTopLevelNode)
  }

  def generateTopLevelNode(node: SyntaxTreeNode): Unit = {
    node match {
      case function: FunctionDeclarationNode =>
        generateTopLevelFunction(function)
      case struct: StructDeclarationNode =>
        generateStructDeclaration(struct)
      case enumeration: EnumDeclarationNode =>
        generateEnumDeclaration(enumeration)
      case _ =>
    }
  }

  def generateStructDeclaration(node: StructDeclarationNode): Unit = {
    module.createStruct(node.name, PaddingSorter.sortTypes(node.fields.map(_.nodeType.referenceDragon)))

    val block = typeEnvironment.root.surfaceSearchChild(node).get

    for (function <- node.functions) {
      generateFunctionDeclaration(
        block = block,
        node = function,
        name = s"${node.name}_${function.name}",
        parameters = function.parameters,
        returnType = function.nodeType.returnType,
        modifiers = function.modifiers
      )
    }
  }

  def generateEnumDeclaration(node: EnumDeclarationNode): Unit = {
    module.createStruct("enum_" + node.name, List(
      DragonType.Int8,
      DragonType.StaticPointer
    ))
  }

  def generateTopLevelFunction(node: FunctionDeclarationNode): Unit = {
    node.modifiers.collectFirst {
      case FunctionModifier.External(externalModule) =>
        module.dependencies += Dependency.Function(
          name = node.name,
          returnType = node.nodeType.returnType.referenceDragon,
          parameters = node.parameters.map(_.nodeType.referenceDragon),
        )
        return
    }
    val block = typeEnvironment.root.surfaceSearchChild(node).get

    generateFunctionDeclaration(
      block = block,
      node = node,
      name = node.name,
      parameters = node.parameters,
      returnType = node.nodeType.returnType,
      modifiers = node.modifiers
    )
  }

  def generateFunctionDeclaration(
    block: SymbolBlock,
    node: FunctionDeclarationNode,
    name: String,
    parameters: List[FunctionParameterNode],
    returnType: Type,
    modifiers: Set[FunctionModifier],
  ): Unit = {
    // TODO: implement better checks here
    val isMain = name == "main"
    val hasReturn = node.children.exists(_.isInstanceOf[ReturnNode])

    val factory = module.createFunction(
      name = name,
      parameters = parameters.map(_.nodeType.referenceDragon),
      returnType = returnType.referenceDragon
    )
    // TODO: remove index-based approach
    factory.function.parameters.zipWithIndex.foreach { case (param, index) =>
      insertMemory(block, parameters(index).name, param)
    }

    node.block.children.foreach { child =>
      generateFunctionInstruction(block, node, factory, child) match {
        case Some(statement) => factory.statement(statement)
        case _ =>
      }
    }
    if (returnType == Type.Void && !hasReturn) {
      if (isMain) {
        factory.function.returnType = DragonType.Int32
        factory.statement(factory.returnStatement(ConstantReference.Number("0", Int32)))
      } else {
        factory.statement(factory.returnStatement(EmptyValue))
      }
    }
  }

  def generateBlock(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: BlockNode
  ): Unit = {
    node.children.foreach { child =>
      generateFunctionInstruction(block, function, factory, child) match {
        case Some(statement) => factory.statement(statement)
        case _ =>
      }
    }
  }

  def generateFunctionInstruction(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: SyntaxTreeNode
  ): Option[DragonStatement] = {
    node match {
      case assignment: AssignmentNode => generateAssignment(block, function, factory, assignment)
      case ret: ReturnNode => generateReturn(block, function, factory, ret)
      case call: FunctionCallNode => generateCall(block, function, factory, call)
      case `if`: IfNode => generateIf(block, function, factory, `if`)
      case _ =>
        log(LogLevel.ERROR, s"Unknown instruction: $node")
        None
    }
  }

  def generateValue(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: SyntaxTreeNode,
    nest: Boolean = true
  ): Option[ValueReference] = {
    val value = node match {
      case number: NumericNode if nest => generateNumberValue(factory, number)
      case number: NumericNode => generateNumber(factory, number)
      case reference: ReferenceNode => generateReference(block, function, factory, reference)
      case string: StringLiteralNode => generateString(factory, string)
      case binaryOp: BinaryOperationNode => generateBinaryOp(factory, binaryOp)
      case call: FunctionCallNode => generateCall(block, function, factory, call)
      case instantiation: StructInstantiationNode => generateStructInstantiation(block, function, factory, instantiation)
      case access: StructFieldAccessNode => generateStructFieldAccess(block, function, factory, access)
      case equality: EqualityNode => generateEquality(block, function, factory, equality)
      case cast: CastNode => generateCast(block, function, factory, cast)
      case `null`: NullNode => generateNull(factory, `null`)
      case _ =>
        log(LogLevel.ERROR, s"Unknown value: $node")
        None
    }
    value match {
      case Some(statement: TypedDragonStatement) => Some(factory.assign(statement))
      case _ => value
    }
  }

  def generateAssignment(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: AssignmentNode
  ): Option[AssignStatement] = {
    val value = generateValue(
      block = block,
      function = function,
      factory = factory,
      node = node.value,
      nest = false
    ).get
    value match {
      case statement: TypedDragonStatement =>
        val memory = factory.nextMemoryReference(node.nodeType.referenceDragon)
        insertMemory(block, node.name, memory)
        Some(factory.assignStatement(memory, statement, constantOverride = None))
      case memory: MemoryReference =>
        insertMemory(block, node.name, memory)
        None
      case _ => None
    }
  }

  def generateReference(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: ReferenceNode
  ): Option[MemoryReference] = {
    memoryReferences.get(block).flatMap(_.get(node.name))
  }

  def generateCall(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: FunctionCallNode
  ): Option[CallStatement] = {
    val arguments = node.arguments.map { argument =>
      generateValue(
        block = block,
        function = function,
        factory = factory,
        node = argument
      ).get
    }

    val returnType = node.nodeType.referenceDragon
    val call = factory.call(
      name = node.name,
      returnType = returnType,
      arguments = arguments
    )
    Some(call)
  }
  
  def generateIf(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: IfNode
  ): Option[DragonStatement] = {
    val condition = generateValue(
      block = block,
      function = function,
      factory = factory,
      node = node.condition
    ).get
    val ifBlock = factory.createBlock()
    val elseBlock = node.elseBlock.map(_ => factory.createBlock())
    val endBlock = factory.createBlock()

    val functionFactory = factory.functionFactory
    functionFactory.setBlockToEntry()
    elseBlock match
      case Some(elseFactory) =>
        functionFactory.branch(condition, ifBlock.block, elseFactory.block)
        generateBlock(block, function, elseFactory, node.elseBlock.get)
        elseFactory.branch(endBlock.block)
      case None => functionFactory.branch(condition, ifBlock.block, endBlock.block)

    functionFactory.resetBlock()
    generateBlock(block, function, ifBlock, node.block)
    ifBlock.branch(endBlock.block)

    None
  }

  def generateNull(
    factory: StatementHolder,
    node: NullNode
  ): Option[ValueReference] =
    Some(ConstantReference.Null(node.nodeType.referenceDragon))

  def generateReturn(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: ReturnNode
  ): Option[DragonStatement] = {
    // TODO: implement better check
    val isMain = function.name == "main"

    val value = node.value match
      case Some(value) => generateValue(
        block = block,
        function = function,
        factory = factory,
        node = value
      ).get
      case None if isMain => ConstantReference.Number("0", Int32)
      case None => EmptyValue
    val returns = factory.returnStatement(value)
    Some(returns)
  }
  
  def generateEquality(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    equality: EqualityNode
  ): Option[ValueReference] = {
    val left = generateValue(
      block = block,
      function = function,
      factory = factory,
      node = equality.left
    ).get
    val right = generateValue(
      block = block,
      function = function,
      factory = factory,
      node = equality.right
    ).get
    val comparison = factory.compareSignedIntegers(left, right, NumericalComparisonType.Equal)
    Some(factory.assign(comparison))
  }

  def generateCast(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: CastNode
  ): Option[ValueReference] =
    val previousType = node.expression match {
      case `null`: NullNode => DragonType.ContextualPointer(DragonType.Int8)
      case _ => node.expression.nodeType.referenceDragon
    }

    // todo: remove when we have a decent type inference
    node.expression.nodeType = node.nodeType
    val value = generateValue(
      block = block,
      function = function,
      factory = factory,
      node = node.expression
    ).get
    val cast = factory.bitcast(value, previousType, node.nodeType.referenceDragon)
    Some(factory.assign(cast))

  def generateStructFieldAccess(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: StructFieldAccessNode
  ): Option[TypedDragonStatement] = {
    val struct = generateValue(
      block = block,
      function = function,
      factory = factory,
      node = node.struct
    ).get
    val structType = node.struct.nodeType.asInstanceOf[Type.Struct]
    val sortedFields = structType.fields.toList.sortBy(-_._2.referenceDragon.bytes).map(_._1)
    val indexOfField = sortedFields.indexOf(node.fieldName)
    val element = factory.getElementAt(
      struct = struct,
      elementType = node.nodeType.referenceDragon,
      index = ConstantReference.Number(indexOfField.toString, Int32)
    )

    Some(factory.assignAndLoadIfImmutable(element).getOrElse(element))
  }

  def generateStructInstantiation(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: StatementHolder,
    node: StructInstantiationNode
  ): Option[MemoryReference] = {
    val structType = node.nodeType.allocationDragon
    val allocation = factory.assign(factory.allocate(
      allocationType = structType,
      alignment = structType.bytes
    ))
    val values = node.arguments.map(arg => generateValue(
      block = block,
      function = function,
      factory = factory,
      node = arg
    ).get).sortBy(-_.dragonType.bytes)

    if (values.exists(_.isInstanceOf[MemoryReference])) {
      values.zipWithIndex.foreach { case (value, index) =>
        val element = factory.assign(factory.getElementAt(
          struct = allocation,
          elementType = values(index).dragonType,
          index = ConstantReference.Number(index.toString, Int32)
        ))
        factory.store(value, element)
      }
    } else {
      val struct = ConstantReference.Struct(
        fields = values,
        dragonType = structType.asInstanceOf[DragonType.Struct]
      )
      factory.store(struct, allocation)
    }
    Some(allocation)
  }

  def generateNumberValue(
    factory: StatementHolder,
    node: NumericNode,
  ): Option[ValueReference] = {
    Some(ConstantReference.Number(node.token.value, node.nodeType.referenceDragon))
  }

  def generateNumber(
    factory: StatementHolder,
    node: NumericNode
  ): Option[BinaryOpStatement] = {
    Some(factory.add(
      ConstantReference.Number(
        number = node.token.value,
        dragonType = node.nodeType.referenceDragon
      ),
      ConstantReference.Number(
        number = "0",
        dragonType = node.nodeType.referenceDragon
      )
    ))
  }

  def generateBinaryOp(
    factory: StatementHolder,
    node: BinaryOperationNode
  ): Option[BinaryOpStatement] = {
    val left = generateValue(
      block = null,
      function = null,
      factory = factory,
      node = node.left
    ).get
    val right = generateValue(
      block = null,
      function = null,
      factory = factory,
      node = node.right
    ).get
    val op = node.op match {
      case BinaryOp.Plus => BinaryOpType.Add
      case BinaryOp.Minus => BinaryOpType.Sub
      case BinaryOp.Multiply => BinaryOpType.Mul
      case BinaryOp.Divide => BinaryOpType.Div
    }

    Some(factory.binaryOp(left, right, op))
  }

  def generateString(
    factory: StatementHolder,
    node: StringLiteralNode
  ): Option[BitcastStatement] = {
    val format = factory.useFormat(
      name = s"str_${node.hashCode.toString}",
      value = convertControlChars(node.token.value)
    )
    Some(format)
  }

  private def insertMemory(block: SymbolBlock, name: String, memory: MemoryReference): Unit = {
    memoryReferences.get(block) match {
      case Some(references) => references(name) = memory
      case None => memoryReferences(block) = mutable.HashMap(name -> memory)
    }
  }

  extension (serenType: Type) {
    def allocationDragon: DragonType = serenType match {
      case Type.Byte => DragonType.Int8
      case Type.Short => DragonType.Int16
      case Type.Int => DragonType.Int32
      case Type.Long => DragonType.Int64
      case Type.Void => DragonType.Void
      case Type.Usize => target.pointerSize match
        case 8 => DragonType.Int8
        case 16 => DragonType.Int16
        case 32 => DragonType.Int32
        case 64 => DragonType.Int64
      case Type.String => DragonType.Array(DragonType.Int8, 0)
      case Type.Pointer(base) => DragonType.ContextualPointer(base.allocationDragon)
      case Type.CType(name) => DragonType.Custom(name)
      case Type.Vararg(_) => DragonType.Vararg(None)
      case Type.Struct(name, fields) => DragonType.Struct(
        name,
        fields.values.map(_.allocationDragon.bytes).lcmOfIterable
      )
      case _ => throw new Exception(s"Unsupported LLVM type $serenType")
    }
    def referenceDragon: DragonType = serenType match {
      case Type.String => DragonType.ContextualPointer(DragonType.Int8)
      case Type.Struct(_, _) => DragonType.ContextualPointer(allocationDragon)
      case _ => allocationDragon
    }
  }
}