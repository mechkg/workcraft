package org.workcraft.graphics
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform

import Java2DDecoration._

case class TouchableC(touchable : Touchable, center : Point2D.Double) {
  def transform(t : AffineTransform) = TouchableC(touchable.transform(t), center.transform(t))
  
  def forJava = new org.workcraft.dom.visual.Touchable {
	override def hitTest(point : Point2D.Double) = touchable.hitTest(point)
	override def getBoundingBox = touchable.boundingBox.rect
	override def getCenter = center
  }
}
