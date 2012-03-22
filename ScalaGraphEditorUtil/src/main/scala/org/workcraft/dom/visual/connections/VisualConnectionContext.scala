package org.workcraft.dom.visual.connections;
import org.workcraft.graphics.TouchableC
import java.awt.geom.Point2D
import org.workcraft.graphics.Touchable

case class VisualConnectionContext(c1 : TouchableC, c2 : TouchableC)

object VisualConnectionContext {
  def makeContext (t1: Touchable, ap1: Point2D.Double, t2: Touchable, ap2: Point2D.Double) = VisualConnectionContext(TouchableC(t1, ap1), TouchableC(t2,ap2)) 
}
