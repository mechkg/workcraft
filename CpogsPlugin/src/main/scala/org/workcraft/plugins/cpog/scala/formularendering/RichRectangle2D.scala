package org.workcraft.plugins.cpog.scala.formularendering

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
class RichRectangle2D(self : Rectangle2D) {
  import self._
	def plus(point : Point2D) : Rectangle2D = {
	  val copy = self.clone.asInstanceOf[Rectangle2D]
	  copy.add(point)
	  copy
	}
	
	def offset (dx: Double, dy : Double) : Rectangle2D = {
	  new Rectangle2D.Double (getX+dx, getY+dy, getWidth, getHeight)
	}
}

object RichRectangle2D {
	implicit def enrich(rect: Rectangle2D) = new RichRectangle2D(rect)
}
