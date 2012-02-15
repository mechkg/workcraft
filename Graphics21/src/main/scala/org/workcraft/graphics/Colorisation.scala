package org.workcraft.graphics
import java.awt.Color

trait Colorisation {
  def getColorisation: Option[Color]
  def getBackground: Option[Color]
}

object Colorisation {
  val Empty = new Colorisation {
    def getColorisation = None
    def getBackground = None
  }
}