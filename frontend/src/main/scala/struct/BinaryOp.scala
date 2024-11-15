package me.gabriel.seren.frontend
package struct

enum BinaryOp:
  case Plus
  case Minus
  case Multiply
  case Divide

  def precedence: Int = this match
    case Plus | Minus => 1
    case Multiply | Divide => 2
    case _ => 0

  def isBinaryOp: Boolean = this match
    case Plus | Minus | Multiply | Divide => true
    case _ => false

  def isUnaryOp: Boolean = this match
    case Plus | Minus => true
    case _ => false

  def isOperator: Boolean = isBinaryOp || isUnaryOp

  def isMatching(other: BinaryOp): Boolean =
    (this, other) match
      case (Plus, Plus) => true
      case (Minus, Minus) => true
      case (Multiply, Multiply) => true
      case (Divide, Divide) => true
end BinaryOp