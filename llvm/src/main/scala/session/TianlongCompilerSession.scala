package me.gabriel.seren.llvm
package session

import `type`.{allocationDragon, referenceDragon}
import util.{PaddingSorter, convertControlChars}

import me.gabriel.seren.analyzer.{SymbolBlock, TypeEnvironment}
import me.gabriel.seren.frontend.parser.Type
import me.gabriel.seren.frontend.parser.tree.*
import me.gabriel.seren.frontend.struct.{BinaryOp, FunctionModifier}
import me.gabriel.seren.frontend.struct.FunctionModifier.External
import me.gabriel.tianlong.TianlongModule
import me.gabriel.tianlong.factory.FunctionFactory
import me.gabriel.tianlong.statement.*
import me.gabriel.tianlong.struct.*
import me.gabriel.tianlong.struct.DragonType.Int32

import scala.collection.mutable

class TianlongCompilerSession(
  val syntaxTree: SyntaxTree,
  val typeEnvironment: TypeEnvironment
) {
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
    if (returnType == Type.Void) {
      factory.statement(factory.returnStatement(EmptyValue))
    }
  }

  def generateFunctionInstruction(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: FunctionFactory,
    node: SyntaxTreeNode
  ): Option[DragonStatement] = {
    node match {
      case assignment: AssignmentNode => generateAssignment(block, function, factory, assignment)
      case ret: ReturnNode => generateReturn(block, function, factory, ret)
      case call: FunctionCallNode => generateCall(block, function, factory, call)
      case _ =>
        println(s"Unknown instruction: $node")
        None
    }
  }

  def generateValue(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: FunctionFactory,
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
      case _ =>
        println(s"Unknown value: $node")
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
    factory: FunctionFactory,
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
    factory: FunctionFactory,
    node: ReferenceNode
  ): Option[MemoryReference] = {
    memoryReferences.get(block).flatMap(_.get(node.name))
  }

  def generateCall(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: FunctionFactory,
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

  def generateReturn(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: FunctionFactory,
    node: ReturnNode
  ): Option[DragonStatement] = {
    val returns = factory.returnStatement(
      value = generateValue(
        block = block,
        function = function,
        factory = factory,
        node = node.value
      ).get
    )
    Some(returns)
  }

  def generateStructFieldAccess(
    block: SymbolBlock,
    function: FunctionDeclarationNode,
    factory: FunctionFactory,
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
    factory: FunctionFactory,
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
    factory: FunctionFactory,
    node: NumericNode,
  ): Option[ValueReference] = {
    Some(ConstantReference.Number(node.token.value, node.nodeType.referenceDragon))
  }

  def generateNumber(
    factory: FunctionFactory,
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
    factory: FunctionFactory,
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
    factory: FunctionFactory,
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
}