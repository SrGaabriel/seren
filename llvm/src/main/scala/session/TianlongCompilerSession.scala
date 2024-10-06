package me.gabriel.seren.llvm
package session

import `type`.dragon

import me.gabriel.seren.analyzer.{SymbolBlock, TypeEnvironment}
import me.gabriel.seren.frontend.parser.tree.{AssignmentNode, FunctionCallNode, FunctionDeclarationNode, NumericNode, ReferenceNode, ReturnNode, StringLiteralNode, SyntaxTree, SyntaxTreeNode}
import me.gabriel.seren.frontend.struct.FunctionModifier
import me.gabriel.seren.frontend.struct.FunctionModifier.External
import me.gabriel.tianlong.TianlongModule
import me.gabriel.tianlong.factory.FunctionFactory
import me.gabriel.tianlong.statement.{AddStatement, AssignStatement, CallStatement, DragonStatement, GetElementPointerStatement, TypedDragonStatement}
import me.gabriel.tianlong.struct.Dependency.Constant
import me.gabriel.tianlong.struct.{ConstantReference, Dependency, MemoryReference, ValueReference}

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
      case declaration: FunctionDeclarationNode =>
        generateFunctionDeclaration(declaration)
      case _ =>
    }
  }

  def generateFunctionDeclaration(node: FunctionDeclarationNode): Unit = {
    node.modifiers.collectFirst {
      case FunctionModifier.External(externalModule) =>
        module.dependencies += Dependency.Function(
          name = node.name,
          returnType = node.returnType.dragon,
          parameters = node.parameters.map(_.nodeType.dragon),
        )
        return
    }

    val block = typeEnvironment.root.surfaceSearchChild(node).get

    val factory = module.createFunction(
      name = node.name,
      parameters = node.parameters.map(_.nodeType.dragon),
      returnType = node.returnType.dragon
    )
    // TODO: remove index-based approach
    factory.function.parameters.zipWithIndex.foreach { case (param, index) =>
      insertMemory(block, node.parameters(index).name, param)
    }

    node.block.children.foreach { child =>
      generateFunctionInstruction(block, node, factory, child) match {
        case Some(statement) => factory.statement(statement)
        case None => None
      }
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
      case number: NumericNode => generateNumber(factory, number)
      case call: FunctionCallNode => generateCall(block, function, factory, call)
      case string: StringLiteralNode => generateString(factory, string)
      case _ =>
        println(s"Unknown node: $node")
        None
    }
  }

  def generateValue(
                      block: SymbolBlock,
                      function: FunctionDeclarationNode,
                      factory: FunctionFactory,
                      node: SyntaxTreeNode
                   ): Option[ValueReference] = {
    node match {
      case reference: ReferenceNode => generateReference(block, function, factory, reference)
      case _ => generateFunctionInstruction(block, function, factory, node) match {
        case Some(statement: TypedDragonStatement) => Some(statement)
        case _ => None
      }
    }
  }
  
  def generateAssignment(
                          block: SymbolBlock,
                          function: FunctionDeclarationNode,
                          factory: FunctionFactory,
                          node: AssignmentNode
                        ): Option[AssignStatement] = {
    val value = generateFunctionInstruction(
      block = block,
      function = function,
      factory = factory,
      node = node.value
    )
    value match {
      case Some(statement: TypedDragonStatement) =>
        val memory = factory.nextMemoryReference(node.nodeType.dragon)
        insertMemory(block, node.name, memory)
        Some(factory.assignStatement(memory, statement, constantOverride = None))
      case _ => None
    }
  }

  def generateReference(
                        block: SymbolBlock,
                        function: FunctionDeclarationNode,
                        factory: FunctionFactory,
                        node: ReferenceNode
                       ): Option[MemoryReference] = {
    memoryReferences.get(block) match {
      case Some(references) => references.get(node.name) match {
        case Some(reference) => Some(reference)
        case None => None
      }
      case None => None
    }
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

    val call = factory.call(
      name = node.name,
      returnType = node.nodeType.dragon,
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
      value = generateFunctionInstruction(
        block = block,
        function = function,
        factory = factory,
        node = node.value
      ).get.asInstanceOf[TypedDragonStatement]
    )
    Some(returns)
  }

  def generateNumber(
                      factory: FunctionFactory,
                      node: NumericNode,
                    ): Option[AddStatement] = {
    Some(factory.add(
      ConstantReference.Number(
        number = node.token.value,
        dragonType = node.nodeType.dragon
      ),
      ConstantReference.Number(
        number = "0",
        dragonType = node.nodeType.dragon
      )
    ))
  }
  
  def generateString(
                      factory: FunctionFactory,
                      node: StringLiteralNode
                    ): Option[GetElementPointerStatement] = {
    val format = factory.useFormat(
      name = node.hashCode.toString,
      value = node.token.value
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