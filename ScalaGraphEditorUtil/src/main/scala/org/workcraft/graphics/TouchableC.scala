package org.workcraft.graphics
import java.awt.geom.Point2D

case class TouchableC(touchable : Touchable, center : Point2D.Double) {
  def forJava = new org.workcraft.dom.visual.Touchable {
	override def hitTest(point : Point2D.Double) = touchable.hitTest(point)
	override def getBoundingBox = touchable.boundingBox.rect
	override def getCenter = center
  }
}
