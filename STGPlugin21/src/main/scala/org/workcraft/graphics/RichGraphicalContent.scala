package org.workcraft.graphics

import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.Path2D
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.graphics.Java2DDecoration._
import org.workcraft.graphics.Graphics.HorizontalAlignment
import org.workcraft.graphics.Graphics.VerticalAlignment
import org.workcraft.graphics.Graphics._
import java.awt.Stroke
import java.awt.Font

case class RichGraphicalContent(val bcgc: BoundedColorisableGraphicalContent, val touchable: TouchableC) {

  def zeroCentered = align(new Rectangle2D.Double(0,0,0,0), HorizontalAlignment.Center,VerticalAlignment.Center).
             overrideCenter(new Point2D.Double(0,0))

  def translate(p : Point2D.Double) = transform(AffineTransform.getTranslateInstance(p.getX(), p.getY()))
             
  def align (to: Rectangle2D.Double, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): RichGraphicalContent =
    transform(alignTransform(touchable.touchable.boundingBox.rect, to, horizontalAlignment, verticalAlignment))

  def align (to: RichGraphicalContent, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): RichGraphicalContent =
    align(to.touchable.touchable.boundingBox.rect, horizontalAlignment, verticalAlignment)

  def transform(x: AffineTransform): RichGraphicalContent =
    new RichGraphicalContent(bcgc.transform(x),
      touchable.transform(x))

  def overrideCenter(center : Point2D.Double) : RichGraphicalContent = {
    copy(touchable = touchable.copy(center = center))
    }

  def over(bot : BoundedColorisableGraphicalContent) : RichGraphicalContent = copy (bcgc = bot.compose(bcgc)) 
  def under(top : BoundedColorisableGraphicalContent) : RichGraphicalContent = copy (bcgc = bcgc.compose(top)) 
  def under(top : Option[BoundedColorisableGraphicalContent]) : RichGraphicalContent = top match {
    case None => this
    case Some(top) => under(top) 
  } 
  
  //def over (x: RichGraphicalContent, touchableOverride: Touchable) = debugOver (x, touchableOverride) */
}
  
object RichGraphicalContent {
  def rectangle(width: Double, height: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) = {
    val rect = Graphics.rectangle(width, height, stroke, fill)
    RichGraphicalContent(rect, TouchableC(rect, new Point2D.Double(0,0)))
  }
  def label(text : String, font : Font, color : Color) : RichGraphicalContent = {
    val lbl = Graphics.label(text, font, color)
    RichGraphicalContent(lbl, TouchableC(lbl, lbl.visualBounds.center))
  }
  def circle(diameter: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) = {
    val circ = Graphics.circle(diameter, stroke, fill)
    RichGraphicalContent(circ, TouchableC(circ, new Point2D.Double(0,0)))
  }
}
  
  /*  

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
    
*/