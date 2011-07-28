package org.workcraft.graphics

import org.workcraft.dom.visual.ColorisableGraphicalContent
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.Path2D
import java.awt.BasicStroke
import org.workcraft.dom.visual.Touchable
import org.workcraft.graphics.Graphics._
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import java.awt.Color

class RichGraphicalContent(val colorisableGraphicalContent: ColorisableGraphicalContent, val visualBounds: Rectangle2D, val touchable: Touchable) {
  def transform(x: AffineTransform): RichGraphicalContent =
    new RichGraphicalContent(Graphics.transform(colorisableGraphicalContent, x),
      Graphics.transform(visualBounds, x),
      TouchableUtil.transform(touchable, x))

  def translate(tx: Double, ty: Double): RichGraphicalContent = transform(AffineTransform.getTranslateInstance(tx, ty))

  def translate(position: Point2D): RichGraphicalContent = transform(AffineTransform.getTranslateInstance(position.getX, position.getY))

  def compose(a: RichGraphicalContent, b: RichGraphicalContent): RichGraphicalContent =
    new RichGraphicalContent(Graphics.compose(a.colorisableGraphicalContent, b.colorisableGraphicalContent),
      a.visualBounds.createUnion(b.visualBounds),
      TouchableUtil.compose(a.touchable, b.touchable))
  
  def compose(a: RichGraphicalContent, b: RichGraphicalContent, touchableOverride: Touchable): RichGraphicalContent =
    new RichGraphicalContent(Graphics.compose(a.colorisableGraphicalContent, b.colorisableGraphicalContent),
      a.visualBounds.createUnion(b.visualBounds),
      touchableOverride)

  def align (to: RichGraphicalContent, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): RichGraphicalContent =
    transform(alignTransform(touchable.getBoundingBox, to.touchable.getBoundingBox, horizontalAlignment, verticalAlignment))

  def alignSideways (relativeTo: RichGraphicalContent, position: LabelPositioning): RichGraphicalContent =
    transform(LabelPositioning.positionRelative(touchable.getBoundingBox, relativeTo.touchable.getBoundingBox, position))

  private def releaseOver(x: RichGraphicalContent) = compose(x, this)
  
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
}

object RichGraphicalContent {
  val empty = new RichGraphicalContent(BoundedColorisableGraphicalContent.EMPTY.graphics, new Rectangle2D.Double(), TouchableUtil.empty)
}