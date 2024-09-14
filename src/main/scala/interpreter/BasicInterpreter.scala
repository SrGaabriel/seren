package me.gabriel.soma
package interpreter

import struct.BinaryOp
import parser.tree.{BinaryOperationNode, NumericNode, SyntaxTree, SyntaxTreeNode}

class BasicInterpreter {
  def execute(tree: SyntaxTree): Unit = {
    tree.root.children.foreach { statement =>
      val result = runStatement(statement)
      println("Running statement: " + result)
    }
  }

  def runStatement(statement: SyntaxTreeNode): Any = {
    statement match {
      case BinaryOperationNode(_,_,_,_) => parseExpression(statement)
      case _ => ()
    }
  }

  def parseExpression(expression: SyntaxTreeNode): Any = {
    expression match {
      case BinaryOperationNode(_,op,left, right) => parseBinaryOp(
        parseExpression(left),
        op,
        parseExpression(right)
      )
      case NumericNode(token, _) => token.value.toInt
      case _ => throw new RuntimeException("Invalid expression")
    }
  }

  def parseBinaryOp(left: Any, op: BinaryOp, right: Any): Any = {
    op match {
      case BinaryOp.Plus => left.asInstanceOf[Int] + right.asInstanceOf[Int]
      case BinaryOp.Minus => left.asInstanceOf[Int] - right.asInstanceOf[Int]
      case BinaryOp.Multiply => left.asInstanceOf[Int] * right.asInstanceOf[Int]
      case BinaryOp.Divide => left.asInstanceOf[Int] / right.asInstanceOf[Int]
    }
  }
}
