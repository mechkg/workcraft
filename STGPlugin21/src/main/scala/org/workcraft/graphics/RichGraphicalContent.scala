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
import org.workcraft.gui.CommonVisualSettings
import scalaz._
import Scalaz._

package object stg {

type RichGraphicalContent = CommonVisualSettings => NotSoRichGraphicalContent
  
object RichGraphicalContent {
  implicit def decorateRGC(rgc : RichGraphicalContent) = new {
  def zeroCentered : RichGraphicalContent = rgc.map(_.zeroCentered)
}
}

case class NotSoRichGraphicalContent(val bcgc: BoundedColorisableGraphicalContent, val touchable: TouchableC) {

  def zeroCentered = align(new Rectangle2D.Double(0,0,0,0), HorizontalAlignment.Center,VerticalAlignment.Center).
             overrideCenter(new Point2D.Double(0,0))

  def translate(p : Point2D.Double) = transform(AffineTransform.getTranslateInstance(p.getX(), p.getY()))
             
  def align (to: Rectangle2D.Double, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): NotSoRichGraphicalContent =
    transform(alignTransform(touchable.touchable.boundingBox.rect, to, horizontalAlignment, verticalAlignment))

  def align (to: NotSoRichGraphicalContent, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): NotSoRichGraphicalContent =
    align(to.touchable.touchable.boundingBox.rect, horizontalAlignment, verticalAlignment)

  def transform(x: AffineTransform): NotSoRichGraphicalContent =
    new NotSoRichGraphicalContent(bcgc.transform(x), touchable.transform(x))

  def overrideCenter(center : Point2D.Double) : NotSoRichGraphicalContent = {
    copy(touchable = touchable.copy(center = center))
    }

  def over(bot : BoundedColorisableGraphicalContent) : NotSoRichGraphicalContent = copy (bcgc = bot.compose(bcgc)) 
  def under(top : BoundedColorisableGraphicalContent) : NotSoRichGraphicalContent = copy (bcgc = bcgc.compose(top)) 
  def under(top : Option[BoundedColorisableGraphicalContent]) : NotSoRichGraphicalContent = copy (bcgc = top match {
    case None => bcgc
    case Some(top) => bcgc compose top 
  })
  
  //def over (x: NotSoRichGraphicalContent, touchableOverride: Touchable) = debugOver (x, touchableOverride) */
}
  
object NotSoRichGraphicalContent {
  def rectangle(width: Double, height: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) = {
    val rect = Graphics.rectangle(width, height, stroke, fill)
    NotSoRichGraphicalContent(rect.boundedColorisableGraphicalContent, TouchableC(rect.touchable, new Point2D.Double(0,0)))
  }
  def label(text : String, font : Font, color : Color) : NotSoRichGraphicalContent = {
    val lbl = Graphics.label(text, font, color)
    NotSoRichGraphicalContent(lbl.boundedColorisableGraphicalContent, TouchableC(lbl.touchable, lbl.visualBounds.center))
  }
  def circle(diameter: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) = {
    val circ = Graphics.circle(diameter, stroke, fill)
    NotSoRichGraphicalContent(circ.boundedColorisableGraphicalContent, TouchableC(circ.touchable, new Point2D.Double(0,0)))
  }
}

  /*  

  def translate(tx: Double, ty: Double): NotSoRichGraphicalContent = transform(AffineTransform.getTranslateInstance(tx, ty))

  def translate(position: Point2D): NotSoRichGraphicalContent = transform(AffineTransform.getTranslateInstance(position.getX, position.getY))

  def compose(b: NotSoRichGraphicalContent): NotSoRichGraphicalContent =
    new NotSoRichGraphicalContent(Graphics.compose(colorisableGraphicalContent, b.colorisableGraphicalContent),
      visualBounds.createUnionD(b.visualBounds),
      TouchableUtil.compose(touchable, b.touchable))
  
  def compose(b: NotSoRichGraphicalContent, touchableOverride: Touchable): NotSoRichGraphicalContent =
    new NotSoRichGraphicalContent(Graphics.compose(colorisableGraphicalContent, b.colorisableGraphicalContent),
      visualBounds.createUnionD(b.visualBounds),
      touchableOverride)

  def alignSideways (relativeTo: NotSoRichGraphicalContent, position: LabelPositioning): NotSoRichGraphicalContent =
    transform(LabelPositioning.positionRelative(touchable.getBoundingBox, relativeTo.touchable.getBoundingBox, position))

  private def releaseOver(x: NotSoRichGraphicalContent) = compose(x, this)
  
  private def releaseOver(x: NotSoRichGraphicalContent, touchableOverride: Touchable) = x.compose(this, touchableOverride)

  private def debugOver (x: NotSoRichGraphicalContent) = {
    val redstroke = new BasicStroke(0.01f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
    val stroke = new BasicStroke (0.01f)
    ((shape (x.touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (x.visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    (shape (touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    x).compose(this, TouchableUtil.compose(touchable, x.touchable))
  }
  
  private def debugOver (x: NotSoRichGraphicalContent, touchableOverride: Touchable) = {
    val redstroke = new BasicStroke(0.01f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
    val stroke = new BasicStroke (0.01f)
    ((shape (x.touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (x.visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    (shape (touchable.getBoundingBox, Some((redstroke,Color.RED)), None)) releaseOver
    (shape (visualBounds, Some((stroke,Color.BLUE)), None)) releaseOver
    x).compose(this, touchableOverride)
  }
    
*/
}