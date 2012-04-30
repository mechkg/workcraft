package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform

import Java2DDecoration._

class BoundingBox(val visual: Rectangle2D.Double, val logical: Rectangle2D.Double) {
  def union(other: BoundingBox) =
    BoundingBox(visual.createUnionD(other.visual), logical.createUnionD(other.logical))

  def expand(x: Double, y: Double) =
    BoundingBox(BoundingBox.expandRect(visual, x, y), BoundingBox.expandRect(logical, x, y))

  def transform(transform: AffineTransform) =
    BoundingBox(BoundingBox.transformRect(visual, transform), BoundingBox.transformRect(logical, transform))
}

object BoundingBox {
  def apply(visual: Rectangle2D.Double) = new BoundingBox (visual, visual)
  def apply(visual: Rectangle2D.Double, logical: Rectangle2D.Double) = new BoundingBox (visual, logical)

  def expandRect(rect: Rectangle2D, x: Double, y: Double) =
    {
      val result = new Rectangle2D.Double()
      result.setRect(rect)

      val x_2 = x / 2.0
      val y_2 = y / 2.0

      result.add(rect.getMinX() - x_2, rect.getMinY() - y_2)
      result.add(rect.getMaxX() + x_2, rect.getMaxY() + y_2)

      result
    }

  def transformRect(rect: Rectangle2D, transformation: AffineTransform) =
    {
      val a = new Point2D.Double(rect.getMinX, rect.getMinY)
      val b = new Point2D.Double(rect.getMaxX, rect.getMaxY)

      transformation.transform(a, a)
      transformation.transform(b, b)

      val minX = math.min(a.x, b.x)
      val minY = math.min(a.y, b.y)

      val maxX = math.max(a.x, b.x)
      val maxY = math.max(a.y, b.y)

      new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY)
    }
}
