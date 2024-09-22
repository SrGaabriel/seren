package me.gabriel.seren.frontend
package lexer

enum NewlineStatus:
  case StatementContinuation // usually followed by a dot
  case Newline
  case None
  