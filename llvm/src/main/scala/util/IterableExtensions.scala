package me.gabriel.seren.llvm
package util

import scala.annotation.tailrec

object IterableExtensions {
  @tailrec
  private def gcd(a: Int, b: Int): Int = {
    if (b == 0) a else gcd(b, a % b)
  }

  private def lcm(a: Int, b: Int): Int = {
    (a * b).abs / gcd(a, b)
  }

  implicit class IterableLCM[T](val iterable: Iterable[T]) {
    def lcmOfIterable(implicit numeric: Numeric[T]): Int = {
      iterable.map(numeric.toInt).reduceLeft(lcm).max(1)
    }
  }
}