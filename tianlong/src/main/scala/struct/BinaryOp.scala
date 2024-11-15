package me.gabriel.tianlong
package struct

enum BinaryOpType(val llvm: String) {
  case Add extends BinaryOpType("add")
  case Sub extends BinaryOpType("sub")
  case Mul extends BinaryOpType("mul")
  case Div extends BinaryOpType("sdiv")
  case Mod extends BinaryOpType("srem")
  case And extends BinaryOpType("and")
  case Or extends BinaryOpType("or")
  case Xor extends BinaryOpType("xor")
  case Shl extends BinaryOpType("shl")
  case Shr extends BinaryOpType("ashr")

  def getName(floatingPoint: Boolean) = {
    if (floatingPoint) {
      this match {
        case Add => "fadd"
        case Sub => "fsub"
        case Mul => "fmul"
        case Div => "fdiv"
        case _ => throw new IllegalArgumentException(s"Invalid float operation: $this")
      }
    } else {
      this.llvm
    }
  }
}