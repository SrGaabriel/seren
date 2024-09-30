package me.gabriel.seren.llvm
package session

import `type`.dragon

import me.gabriel.seren.analyzer.TypeEnvironment
import me.gabriel.seren.frontend.parser.tree.{FunctionDeclarationNode, SyntaxTree, SyntaxTreeNode}
import me.gabriel.tianlong.TianlongModule
import me.gabriel.tianlong.struct.ValueReference

class TianlongCompilerSession(
                             val syntaxTree: SyntaxTree,
                             val typeEnvironment: TypeEnvironment
                             ) {
  val module = TianlongModule()

  def finish(): TianlongModule = module

  def generateTopLevelNode(node: SyntaxTreeNode): Unit = {
    node match {
      case declaration: FunctionDeclarationNode =>
        generateFunctionDeclaration(declaration)
      case _ => None
    }
  }
  
  def generateFunctionDeclaration(node: FunctionDeclarationNode): Unit = {
    val block = typeEnvironment.root.surfaceSearchChild(node)
    val factory = module.createFunction(
      name = "main",
      parameters = node.parameters.map(_.nodeType.dragon),
      returnType = node.returnType.dragon
    )
    
    node.block.children.foreach(generateFunctionInstruction)
  }
  
  def generateFunctionInstruction(node: SyntaxTreeNode): Option[ValueReference] = {
    None
  }
}
