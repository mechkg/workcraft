package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform

trait Touchable {
  def hitTest(point: Point2D): Boolean
  def boundingBox: BoundingBox

  def compose(other: Touchable) = {
    val outer = this
    new Touchable {
      def hitTest(point: Point2D) = outer.hitTest(point) || other.hitTest(point)
      def boundingBox = outer.boundingBox.union(other.boundingBox)
    }
  }

  def transform(transformation: AffineTransform) = {
    val outer = this

    new Touchable {
      val inverse = transformation.createInverse

      def hitTest(point: Point2D) = {
        val p = new Point2D.Double
        inverse.transform(point, p)
        outer.hitTest(p)
      }

      def boundingBox = outer.boundingBox.transform(transformation)
    }
  }
}

object Touchable {
  def fromRect (rect: Rectangle2D) = new Touchable {
    def hitTest(p:Point2D) = rect.contains(p.getX, p.getY)
    def boundingBox = BoundingBox(rect)
  }  
}