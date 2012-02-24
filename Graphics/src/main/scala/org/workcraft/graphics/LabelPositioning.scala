package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import java.awt.geom.AffineTransform

sealed abstract class LabelPositioning (val name: String, val dx: Int, val dy: Int)

object LabelPositioning {
  case object Top extends LabelPositioning ("Top", 0, -1) 
  case object Left extends LabelPositioning ("Left", -1, 0)
  case object Right extends LabelPositioning ("Right", 1, 0)
  case object Bottom extends LabelPositioning ("Bottom", 0, 1)
  case object Center extends LabelPositioning ("Center", 0, 0)
  
  def values = List(Top, Left, Right, Bottom, Center)
  
  def getChoice = values.map (v => (v.name, v))
  
  def positionRelative (what: Rectangle2D, relativeTo: Rectangle2D, positioning: LabelPositioning) = {
		val tx = -what.getCenterX() + relativeTo.getCenterX() + 0.5 * positioning.dx * (relativeTo.getWidth() + what.getWidth() + 0.2)
		val ty = -what.getCenterY() + relativeTo.getCenterY() + 0.5 * positioning.dy * (relativeTo.getHeight() + what.getHeight() + 0.2)
		
		AffineTransform.getTranslateInstance(tx, ty)
  }
}