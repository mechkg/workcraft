package org.workcraft.graphics
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform

import Java2DDecoration._

case class TouchableC(touchable : Touchable, center : Point2D.Double) {
  def transform(t : AffineTransform) = TouchableC(touchable.transform(t), center.transform(t))
}
