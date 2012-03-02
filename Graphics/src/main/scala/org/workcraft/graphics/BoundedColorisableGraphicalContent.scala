package org.workcraft.graphics

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.AffineTransform
import org.workcraft.graphics.Graphics.HorizontalAlignment
import org.workcraft.graphics.Graphics.VerticalAlignment
import org.workcraft.graphics.Graphics.alignTransform

class BoundedColorisableGraphicalContent(val cgc: ColorisableGraphicalContent, val bounds: BoundingBox) {
  //def translateToZero = translate(new Point2D.Double(-bounds.pivot.getX, -bounds.pivot.getY()))
  
  def centerToBoundingBox = translate(-bounds.rect.getCenterX(), -bounds.rect.getCenterY())
 
  def translate(offsetX: Double, offsetY: Double) = transform(AffineTransform.getTranslateInstance(offsetX, offsetY))
  
  def transform(transform: AffineTransform) = {
    BoundedColorisableGraphicalContent(
      cgc.transform(transform),
      bounds.transform(transform))
  }
  
  def compose(top: BoundedColorisableGraphicalContent) =
    BoundedColorisableGraphicalContent(
      cgc.compose(top.cgc),
      bounds.union(top.bounds))
      
def align (to: BoundedColorisableGraphicalContent, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): BoundedColorisableGraphicalContent =
    transform(alignTransform(bounds.rect, to.bounds.rect, horizontalAlignment, verticalAlignment))

  def alignSideways (relativeTo: BoundedColorisableGraphicalContent, position: LabelPositioning): BoundedColorisableGraphicalContent =
    transform(LabelPositioning.positionRelative(bounds.rect, relativeTo.bounds.rect, position))
}

object BoundedColorisableGraphicalContent {
/*
 * Empty BCGC does not make sense because being "bounded" with an undefined bounding box
 * means that it is just CGC
 *
 * That's like saying 3 is not a rational number because it's merely an integer
 *
  val Empty = BoundedColorisableGraphicalContent(ColorisableGraphicalContent.Empty, None)
*/
  
  def apply(cgc: ColorisableGraphicalContent, bounds: BoundingBox) = new BoundedColorisableGraphicalContent(cgc, bounds)
}
