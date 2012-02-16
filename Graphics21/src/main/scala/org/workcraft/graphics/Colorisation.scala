package org.workcraft.graphics
import java.awt.Color

case class Colorisation (foreground: Option[Color], background: Option[Color])

object Colorisation {
  val Empty = new Colorisation(None, None)
}