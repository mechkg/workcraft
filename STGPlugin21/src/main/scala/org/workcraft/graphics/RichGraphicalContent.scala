package org.workcraft.graphics

import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.Path2D
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.graphics.Java2DDecoration._

case class RichGraphicalContent(val bcgc: BoundedColorisableGraphicalContent, val touchable: TouchableC) {
/*  def transform(x: AffineTransform): RichGraphicalContent =
    new RichGraphicalContent(Graphics.transform(colorisableGraphicalContent, x),
      Graphics.transform(visualBounds, x),
      TouchableUtil.transform(touchable, x))

  def translate(tx: Double, ty: Double): RichGraphicalContent = transform(AffineTransform.getTranslateInstance(tx, ty))

  def translate(position: Point2D): RichGraphicalContent = transform(AffineTransform.getTranslateInstance(position.getX, position.getY))

  def compose(b: RichGraphicalContent): RichGraphicalContent =
    new RichGraphicalContent(Graphics.compose(colorisableGraphicalContent, b.colorisableGraphicalContent),
      visualBounds.createUnionD(b.visualBounds),
      TouchableUtil.compose(touchable, b.touchable))
  
  def compose(b: RichGraphicalContent, touchableOverride: Touchable): RichGraphicalContent =
    new RichGraphicalContent(Graphics.compose(colorisableGraphicalContent, b.colorisableGraphicalContent),
      visualBounds.createUnionD(b.visualBounds),
      touchableOverride)

  def overrideCenter(center : Point2D.Double) : RichGraphicalContent = {
    copy(touchable = new Touchable {
	  def hitTest(point : Point2D.Double) = touchable.hitTest(point)
	  def getBoundingBox = touchable.getBoundingBox
	  def getCenter = center
    })
  }
  
  def zeroCentered = align(rectangle(0,0,None,None), HorizontalAlignment.Center,VerticalAlignment.Center).
             overrideCenter(new Point2D.Double(0,0))

  def align (to: RichGraphicalContent, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): RichGraphicalContent =
    transform(alignTransform(touchable.getBoundingBox, to.touchable.getBoundingBox, horizontalAlignment, verticalAlignment))

  def alignSideways (relativeTo: RichGraphicalContent, position: LabelPositioning): RichGraphicalContent =
    transform(LabelPositioning.positionRelative(touchable.getBoundingBox, relativeTo.touchable.getBoundingBox, position))

  private def releaseOver(x: RichGraphicalContent) = compose(x, this)
  
  private def releaseOver(x: RichGraphicalContent, touchableOverride: Touchable) = x.compose(this, touchableOverride)

  private def debugOver (x: RichGraphicalContent) = {
    val redstroke = new BasicStroke(0.01f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
    val stroke = new BasicStroke (0.01f)
    ((shape (x.touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (x.visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    (shape (touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    x).compose(this, TouchableUtil.compose(touchable, x.touchable))
  }
  
  private def debugOver (x: RichGraphicalContent, touchableOverride: Touchable) = {
    val redstroke = new BasicStroke(0.01f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
    val stroke = new BasicStroke (0.01f)
    ((shape (x.touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (x.visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    (shape (touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    x).compose(this, touchableOverride)
  }
    
  def over (x: RichGraphicalContent) = debugOver (x)
  def over (x: RichGraphicalContent, touchableOverride: Touchable) = debugOver (x, touchableOverride) */
}
