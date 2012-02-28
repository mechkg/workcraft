package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform

case class PivotedBoundingBox(boundingBox: BoundingBox, pivot: Point2D) {
  def union(other: PivotedBoundingBox, newPivot: Point2D) =
    PivotedBoundingBox(boundingBox.union(other.boundingBox), newPivot)

  def expand(x: Double, y: Double, newPivot: Point2D) = 
    PivotedBoundingBox(boundingBox.expand(x, y), newPivot)
    
  def transform(transform: AffineTransform) = 
    PivotedBoundingBox(boundingBox.transform(transform), PivotedBoundingBox.transformPoint(pivot, transform))
  
  def transformAroundPivot(transform: AffineTransform) = {
    // Java API is so ugly    
    val t = AffineTransform.getTranslateInstance(-pivot.getX, -pivot.getY) // align pivot with origin
    t.concatenate(transform) // apply transform
    t.concatenate(AffineTransform.getTranslateInstance(pivot.getX, pivot.getY)) // move pivot back where it was
    
    t
  }
}

object PivotedBoundingBox {
  def transformPoint (p: Point2D, t: AffineTransform) = {
    val result = new Point2D.Double
    t.transform(p, result)
    result
  }
}