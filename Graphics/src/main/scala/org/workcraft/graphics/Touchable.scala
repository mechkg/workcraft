package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform


trait Touchable {
  def hitTest(point: Point2D.Double): Boolean
  def boundingBox: BoundingBox

  def compose(other: Touchable) = {
    val outer = this
    new Touchable {
      def hitTest(point: Point2D.Double) = outer.hitTest(point) || other.hitTest(point)
      def boundingBox = outer.boundingBox.union(other.boundingBox)
    }
  }

  def transform(transformation: AffineTransform) = {
    val outer = this

    new Touchable {
      val inverse = transformation.createInverse

      def hitTest(point: Point2D.Double) = {
        val p = new Point2D.Double
        inverse.transform(point, p)
        outer.hitTest(p)
      }

      def boundingBox = outer.boundingBox.transform(transformation)
    }
  }
}

object Touchable {
  def fromRect (rect: Rectangle2D.Double) = new Touchable {
    def hitTest(p:Point2D.Double) = rect.contains(p.getX, p.getY)
    def boundingBox = BoundingBox(rect)
  }
  
  def fromShape (shape: java.awt.Shape) = new Touchable {
    def hitTest(p: Point2D.Double) = shape.contains(p)
    def boundingBox = BoundingBox(Java2DDecoration.doubulizeRect(shape.getBounds2D))
  }
  
  def fromCircle (radius: Double) = new Touchable {
    def hitTest(p: Point2D.Double) = p.distanceSq(0, 0) <= radius*radius
    def boundingBox = BoundingBox(new Rectangle2D.Double(-radius, -radius, radius*2, radius*2))
  }
}