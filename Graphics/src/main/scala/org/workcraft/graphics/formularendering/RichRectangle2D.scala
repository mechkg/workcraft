package org.workcraft.graphics.formularendering

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
class RichRectangle2D(self : Rectangle2D.Double) {
  import self._
	def plus(point : Point2D.Double) : Rectangle2D.Double = {
	  val copy = self.clone.asInstanceOf[Rectangle2D.Double]
	  copy.add(point)
	  copy
	}
	
	def offset (dx: Double, dy : Double) : Rectangle2D.Double = {
	  new Rectangle2D.Double (getX+dx, getY+dy, getWidth, getHeight)
	}
	def createUnion(other : Rectangle2D.Double) = self.createUnion(other).asInstanceOf[Rectangle2D.Double]
	def createUnionD(other : Rectangle2D.Double) = createUnion(other) 
	def center = new Point2D.Double(getCenterX, getCenterY)
}
