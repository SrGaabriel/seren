package me.gabriel.seren.logging
package effect

import scala.language.implicitConversions

object ConsoleColors extends Enumeration {
  case class AnsiColorCode(code: String) extends super.Val {
    override def toString(): String = code

    def paint(text: String): String = s"$code$text${ConsoleColors.RESET}"
  }

  implicit def valueToColor(x: Value): AnsiColorCode = x.asInstanceOf[AnsiColorCode]

  // Regular Colors (3/4 bit)
  val BLACK: AnsiColorCode = AnsiColorCode("\u001b[30m")
  val RED: AnsiColorCode = AnsiColorCode("\u001b[31m")
  val GREEN: AnsiColorCode = AnsiColorCode("\u001b[32m")
  val YELLOW: AnsiColorCode = AnsiColorCode("\u001b[33m")
  val BLUE: AnsiColorCode = AnsiColorCode("\u001b[34m")
  val MAGENTA: AnsiColorCode = AnsiColorCode("\u001b[35m")
  val CYAN: AnsiColorCode = AnsiColorCode("\u001b[36m")
  val WHITE: AnsiColorCode = AnsiColorCode("\u001b[37m")
  val DEFAULT: AnsiColorCode = AnsiColorCode("\u001b[39m")

  // Bright Colors (3/4 bit)
  val BRIGHT_BLACK: AnsiColorCode = AnsiColorCode("\u001b[90m")
  val BRIGHT_RED: AnsiColorCode = AnsiColorCode("\u001b[91m")
  val BRIGHT_GREEN: AnsiColorCode = AnsiColorCode("\u001b[92m")
  val BRIGHT_YELLOW: AnsiColorCode = AnsiColorCode("\u001b[93m")
  val BRIGHT_BLUE: AnsiColorCode = AnsiColorCode("\u001b[94m")
  val BRIGHT_MAGENTA: AnsiColorCode = AnsiColorCode("\u001b[95m")
  val BRIGHT_CYAN: AnsiColorCode = AnsiColorCode("\u001b[96m")
  val BRIGHT_WHITE: AnsiColorCode = AnsiColorCode("\u001b[97m")

  // Background Colors (3/4 bit)
  val BG_BLACK: AnsiColorCode = AnsiColorCode("\u001b[40m")
  val BG_RED: AnsiColorCode = AnsiColorCode("\u001b[41m")
  val BG_GREEN: AnsiColorCode = AnsiColorCode("\u001b[42m")
  val BG_YELLOW: AnsiColorCode = AnsiColorCode("\u001b[43m")
  val BG_BLUE: AnsiColorCode = AnsiColorCode("\u001b[44m")
  val BG_MAGENTA: AnsiColorCode = AnsiColorCode("\u001b[45m")
  val BG_CYAN: AnsiColorCode = AnsiColorCode("\u001b[46m")
  val BG_WHITE: AnsiColorCode = AnsiColorCode("\u001b[47m")
  val BG_DEFAULT: AnsiColorCode = AnsiColorCode("\u001b[49m")

  // Bright Background Colors (3/4 bit)
  val BG_BRIGHT_BLACK: AnsiColorCode = AnsiColorCode("\u001b[100m")
  val BG_BRIGHT_RED: AnsiColorCode = AnsiColorCode("\u001b[101m")
  val BG_BRIGHT_GREEN: AnsiColorCode = AnsiColorCode("\u001b[102m")
  val BG_BRIGHT_YELLOW: AnsiColorCode = AnsiColorCode("\u001b[103m")
  val BG_BRIGHT_BLUE: AnsiColorCode = AnsiColorCode("\u001b[104m")
  val BG_BRIGHT_MAGENTA: AnsiColorCode = AnsiColorCode("\u001b[105m")
  val BG_BRIGHT_CYAN: AnsiColorCode = AnsiColorCode("\u001b[106m")
  val BG_BRIGHT_WHITE: AnsiColorCode = AnsiColorCode("\u001b[107m")

  // Special Formatting
  val RESET: AnsiColorCode = AnsiColorCode("\u001b[0m")
  val BOLD: AnsiColorCode = AnsiColorCode("\u001b[1m")
  val DIM: AnsiColorCode = AnsiColorCode("\u001b[2m")
  val ITALIC: AnsiColorCode = AnsiColorCode("\u001b[3m")
  val UNDERLINE: AnsiColorCode = AnsiColorCode("\u001b[4m")
  val BLINK: AnsiColorCode = AnsiColorCode("\u001b[5m")
  val RAPID_BLINK: AnsiColorCode = AnsiColorCode("\u001b[6m")
  val REVERSED: AnsiColorCode = AnsiColorCode("\u001b[7m")
  val HIDDEN: AnsiColorCode = AnsiColorCode("\u001b[8m")
  val STRIKETHROUGH: AnsiColorCode = AnsiColorCode("\u001b[9m")

  val READABLE_GRAY: AnsiColorCode = colorRGB(114,114,114)

  // Helper methods for 8-bit colors (256 colors)
  def color256(n: Int): AnsiColorCode = {
    require(n >= 0 && n < 256, "8-bit color code must be between 0 and 255")
    AnsiColorCode(s"\u001b[38;5;${n}m")
  }

  def bgColor256(n: Int): AnsiColorCode = {
    require(n >= 0 && n < 256, "8-bit color code must be between 0 and 255")
    AnsiColorCode(s"\u001b[48;5;${n}m")
  }

  // Helper methods for 24-bit true colors (RGB)
  def colorRGB(r: Int, g: Int, b: Int): AnsiColorCode = {
    require(r >= 0 && r < 256 && g >= 0 && g < 256 && b >= 0 && b < 256,
      "RGB values must be between 0 and 255")
    AnsiColorCode(s"\u001b[38;2;$r;$g;${b}m")
  }

  def bgColorRGB(r: Int, g: Int, b: Int): AnsiColorCode = {
    require(r >= 0 && r < 256 && g >= 0 && g < 256 && b >= 0 && b < 256,
      "RGB values must be between 0 and 255")
    AnsiColorCode(s"\u001b[48;2;$r;$g;${b}m")
  }
}