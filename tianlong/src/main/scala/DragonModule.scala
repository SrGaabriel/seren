package me.gabriel.tianlong

import me.gabriel.tianlong.struct.Dependency

import scala.collection.mutable

trait DragonModule {
  val dependencies: mutable.ListBuffer[Dependency] = mutable.ListBuffer()
}
