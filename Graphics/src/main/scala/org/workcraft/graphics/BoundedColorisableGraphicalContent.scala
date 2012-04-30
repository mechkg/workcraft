package org.workcraft.graphics

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.AffineTransform

import Graphics._

class BoundedColorisableGraphicalContent(val cgc: ColorisableGraphicalContent, val bounds: BoundingBox) {
  //def translateToZero = translate(new Point2D.Double(-bounds.pivot.getX, -bounds.pivot.getY()))
  
  def centerToBoundingBox = translate(-bounds.visual.getCenterX(), -bounds.visual.getCenterY())
 
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
    transform(alignTransform(bounds.visual, to.bounds.visual, horizontalAlignment, verticalAlignment))

  def alignSideways (relativeTo: BoundedColorisableGraphicalContent, position: LabelPositioning): BoundedColorisableGraphicalContent =
    transform(LabelPositioning.positionRelative(bounds.visual, relativeTo.bounds.visual, position))
}

object BoundedColorisableGraphicalContent {
/*
 * Empty BCGC does not make sense because being "bounded" with an undefined bounding box
 * means that it is just CGC
 *
 * That's like saying 3 is not a rational number because it's merely an integer
 * 
 * 3 is 3/1, not 3/NULL
 *
  val Empty = BoundedColorisableGraphicalContent(ColorisableGraphicalContent.Empty, None)
*/
  
  def apply(cgc: ColorisableGraphicalContent, bounds: BoundingBox) = new BoundedColorisableGraphicalContent(cgc, bounds)
}
