package org.workcraft.graphics


import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.Path2D
import java.awt.BasicStroke

import org.workcraft.graphics.Graphics._
import java.awt.Color

case class RichGraphicalContent(bcgc: BoundedColorisableGraphicalContent, touchable: Touchable) {
  
  def transform(x: AffineTransform) =
    RichGraphicalContent(
        bcgc.transform(x),
        touchable.transform(x)
        )

  def translate(tx: Double, ty: Double) = transform(AffineTransform.getTranslateInstance(tx, ty))

  def translate(offset: Point2D) = transform(AffineTransform.getTranslateInstance(offset.getX, offset.getY))

  def compose(top: RichGraphicalContent, newPivot: (Point2D, Point2D) => Point2D, newTouchable: (Touchable, Touchable) => Touchable) =
    RichGraphicalContent(
        bcgc.compose(top.bcgc, newPivot),
        newTouchable(touchable, top.touchable))

  def align (to: RichGraphicalContent, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): RichGraphicalContent =
    transform(alignTransform(bcgc.bounds.boundingBox.rect, to.bcgc.bounds.boundingBox.rect, horizontalAlignment, verticalAlignment))

  def alignSideways (relativeTo: RichGraphicalContent, position: LabelPositioning): RichGraphicalContent =
    transform(LabelPositioning.positionRelative(bcgc.bounds.boundingBox.rect, relativeTo.bcgc.bounds.boundingBox.rect, position))

    /*
  private def releaseOver(x: RichGraphicalContent) = compose(x)
  
  private def releaseOver(x: RichGraphicalContent, touchableOverride: Touchable) = compose(x, this, touchableOverride)

  private def debugOver (x: RichGraphicalContent) = {
    val redstroke = new BasicStroke(0.01f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
    val stroke = new BasicStroke (0.01f)
    compose (
    (shape (x.touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (x.visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    (shape (touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    x, this, TouchableUtil.compose(touchable, x.touchable))
  }
  
  private def debugOver (x: RichGraphicalContent, touchableOverride: Touchable) = {
    val redstroke = new BasicStroke(0.01f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
    val stroke = new BasicStroke (0.01f)
    compose (
    (shape (x.touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (x.visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    (shape (touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    x, this, touchableOverride)
  }
    
  def over (x: RichGraphicalContent) = debugOver (x)
  def over (x: RichGraphicalContent, touchableOverride: Touchable) = debugOver (x, touchableOverride)
  */
}

/*
object RichGraphicalContent {
  val Empty = RichGraphicalContent(ColorisableGraphicalContent.Empty, new Rectangle2D.Double(), TouchableUtil.empty)
}*/

