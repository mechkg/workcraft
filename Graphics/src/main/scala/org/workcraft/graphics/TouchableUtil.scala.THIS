package org.workcraft.graphics
import org.workcraft.dom.visual.Touchable
import java.awt.geom.AffineTransform
import org.workcraft.dom.visual.TransformHelper
import java.awt.geom.Point2D
import java.awt.geom.Path2D
import java.awt.geom.Line2D
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D

import org.workcraft.graphics.Java2DDecoration._

object TouchableUtil {
  def transform(touchable: Touchable, transformation: AffineTransform): Touchable = TransformHelper.transform(touchable, transformation)

  def compose(a: Touchable, b: Touchable) = new Touchable {
    override def hitTest(point: Point2D.Double) = a.hitTest(point) || b.hitTest(point)
    override def getBoundingBox = a.getBoundingBox.createUnionD(b.getBoundingBox)
    override def getCenter = a.getCenter
  }

  def fromRectangle(bb: Rectangle2D.Double) = new Touchable {
    override def hitTest(point: Point2D.Double) = bb.contains(point)
    override def getBoundingBox = bb
    override def getCenter = new Point2D.Double(0, 0);
  }
  
  val empty = new Touchable {
    override def hitTest(point: Point2D.Double) = false
    override def getBoundingBox = new Rectangle2D.Double
    override def getCenter = new Point2D.Double(0, 0);
  }
}
