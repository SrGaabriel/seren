package me.gabriel.seren.llvm
package session

import `type`.dragon

import me.gabriel.seren.analyzer.{SymbolBlock, TypeEnvironment}
import me.gabriel.seren.frontend.parser.tree.{AssignmentNode, FunctionCallNode, FunctionDeclarationNode, NumericNode, ReturnNode, StringLiteralNode, SyntaxTree, SyntaxTreeNode}
import me.gabriel.tianlong.TianlongModule
import me.gabriel.tianlong.factory.FunctionFactory
import me.gabriel.tianlong.statement.{AddStatement, AssignStatement, CallStatement, DragonStatement, GetElementPointerStatement, TypedDragonStatement}
import me.gabriel.tianlong.struct.{ConstantReference, ValueReference}

class TianlongCompilerSession(
                             val syntaxTree: SyntaxTree,
                             val typeEnvironment: TypeEnvironment
                             ) {
  val module = TianlongModule()

  def finish(): TianlongModule = module

  def generateTree(): Unit = {
    syntaxTree.root.children.foreach(generateTopLevelNode)
  }

  def generateTopLevelNode(node: SyntaxTreeNode): Unit = {
    node match {
      case declaration: FunctionDeclarationNode =>
        generateFunctionDeclaration(declaration)
      case _ => None
    }
  }

  def generateFunctionDeclaration(node: FunctionDeclarationNode): Unit = {
    val block = typeEnvironment.root.surfaceSearchChild(node).get
    val factory = module.createFunction(
      name = node.name,
      parameters = node.parameters.map(_.nodeType.dragon),
      returnType = node.returnType.dragon
    )

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
      case _ => None
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
        Some(factory.assignStatement(memory, statement, constantOverride = None))
      case _ => None
    }
  }

  def generateCall(
                    block: SymbolBlock,
                    function: FunctionDeclarationNode,
                    factory: FunctionFactory,
                    node: FunctionCallNode
                  ): Option[CallStatement] = {
    val arguments = node.arguments.map { argument =>
      generateFunctionInstruction(
        block = block,
        function = function,
        factory = factory,
        node = argument
      ).get.asInstanceOf[TypedDragonStatement]
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
}