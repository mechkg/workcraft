package org.workcraft.graphics
import java.awt.Color

object Coloriser {
  private val comp1 = new Array[Float](4)
  private val comp2 = new Array[Float](4)
  private val comp3 = new Array[Float](4)

  private def blend(col: Float, orig: Float) = col + (1.0f - col) * orig * 0.8f

  def colorise(originalColor: Color, colorisation: Option[Color]) = colorisation match {
    case None => originalColor
    case Some(color) => {
      originalColor.getComponents(comp1)
      color.getComponents(comp2)

      comp3(0) = blend(comp2(0), comp1(0))
      comp3(1) = blend(comp2(1), comp1(1))
      comp3(2) = blend(comp2(2), comp1(2))
      comp3(3) = comp1(3)

      new Color(comp3(0), comp3(1), comp3(2), comp3(3))
    }
  }
}