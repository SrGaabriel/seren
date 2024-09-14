package me.gabriel.seren.frontend
package struct

enum BinaryOp:
  case Plus
  case Minus
  case Multiply
  case Divide
  case Power

  def precedence: Int = this match
    case Plus | Minus => 1
    case Multiply | Divide => 2
    case Power => 3
    case _ => 0

  def isLeftAssociative: Boolean = this match
    case Plus | Minus | Multiply | Divide => true
    case Power => false
    case _ => false

  def isRightAssociative: Boolean = !isLeftAssociative

  def isBinaryOp: Boolean = this match
    case Plus | Minus | Multiply | Divide | Power => true
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
      case (Power, Power) => true
end BinaryOp