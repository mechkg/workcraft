package org.workcraft.graphics

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.AffineTransform

case class BoundedColorisableGraphicalContent(cgc: ColorisableGraphicalContent, bounds: PivotedBoundingBox) {
  def translateToZero = translate(new Point2D.Double(-bounds.pivot.getX, -bounds.pivot.getY()))

  def translate(offset: Point2D) = transform(AffineTransform.getTranslateInstance(offset.getX, offset.getY))

  def compose(top: BoundedColorisableGraphicalContent, newPivot: (Point2D, Point2D) => Point2D) =
    BoundedColorisableGraphicalContent(
      cgc.compose(top.cgc),
      bounds.union(top.bounds, newPivot(bounds.pivot, top.bounds.pivot)))

  def transform(transform: AffineTransform) = {
    val t = bounds.transformAroundPivot(transform)

    BoundedColorisableGraphicalContent(
      cgc.transform(t),
      bounds.transform(transform))
  }
}

/*
 * Empty BCGC does not make sense because being "bounded" with an undefined bounding box
 * means that it is just CGC
 *
object BoundedColorisableGraphicalContent {
  val Empty = BoundedColorisableGraphicalContent(ColorisableGraphicalContent.Empty, None)
}
*/