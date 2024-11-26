package me.gabriel.tianlong
package struct

enum LinkageType(val llvm: String):
  case External extends LinkageType("external")
  case Internal extends LinkageType("internal")
  case Private extends LinkageType("private")
  case Weak extends LinkageType("weak")
  case WeakODR extends LinkageType("weak_odr")
  case LinkOnce extends LinkageType("linkonce")
  case LinkOnceODR extends LinkageType("linkonce_odr")