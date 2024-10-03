package me.gabriel.seren.llvm
package session

import `type`.dragon

import me.gabriel.seren.analyzer.{SymbolBlock, TypeEnvironment}
import me.gabriel.seren.frontend.parser.tree.{AssignmentNode, FunctionDeclarationNode, NumericNode, ReturnNode, StringLiteralNode, SyntaxTree, SyntaxTreeNode}
import me.gabriel.tianlong.TianlongModule
import me.gabriel.tianlong.factory.FunctionFactory
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
      generateFunctionInstruction(block, node, factory, false, child)
    }
  }

  def generateFunctionInstruction(
                                   block: SymbolBlock,
                                   function: FunctionDeclarationNode,
                                   factory: FunctionFactory,
                                   store: Boolean,
                                   node: SyntaxTreeNode
                                 ): Option[ValueReference] = {
    node match {
      case assignment: AssignmentNode => generateAssignment(block, function, factory, assignment)
      case ret: ReturnNode => generateReturn(block, function, factory, ret)
      case number: NumericNode => generateNumber(factory, number)
      case string: StringLiteralNode => generateString(factory, string)
      case _ => None
    }
  }

  def generateAssignment(
                          block: SymbolBlock,
                          function: FunctionDeclarationNode,
                          factory: FunctionFactory,
                          node: AssignmentNode
                        ): Option[ValueReference] = {
    generateFunctionInstruction(
      block = block,
      function = function,
      factory = factory,
      store = true,
      node = node.value
    )
  }

  def generateReturn(
                      block: SymbolBlock,
                      function: FunctionDeclarationNode,
                      factory: FunctionFactory,
                      node: ReturnNode
                    ): Option[ValueReference] = {
    factory.`return`(
      value = generateFunctionInstruction(
        block = block,
        function = function,
        factory = factory,
        store = false,
        node = node.value
      ).get
    )
    None
  }

  def generateNumber(
                      factory: FunctionFactory,
                      node: NumericNode,
                    ): Option[ValueReference] = {
    val addition = factory.add(
      ConstantReference.Number(
        number = node.token.value,
        dragonType = node.nodeType.dragon
      ),
      ConstantReference.Number(
        number = "0",
        dragonType = node.nodeType.dragon
      )
    )
    Some(factory.assign(addition))
  }
  
  def generateString(
                      factory: FunctionFactory,
                      node: StringLiteralNode
                    ): Option[ValueReference] = {
    val format = factory.useFormat(
      name = node.hashCode.toString,
      value = node.token.value
    )
    
    Some(factory.assign(format, constantOverride = Some(true)))
  }
}