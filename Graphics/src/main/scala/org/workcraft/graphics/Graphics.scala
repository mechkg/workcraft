import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.Stroke
import java.awt.Color
import java.awt.BasicStroke
import java.awt.Shape
import java.awt.Font
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import java.awt.geom.Path2D

import java.awt.geom.PathIterator

import scalaz.NonEmptyList

package org.workcraft.graphics {
  object Graphics {
    sealed trait HorizontalAlignment
    object HorizontalAlignment {
      case object Left extends HorizontalAlignment
      case object Right extends HorizontalAlignment
      case object Center extends HorizontalAlignment
      case object None extends HorizontalAlignment
    }

    sealed trait VerticalAlignment
    object VerticalAlignment {
      case object Top extends VerticalAlignment
      case object Bottom extends VerticalAlignment
      case object Center extends VerticalAlignment
      case object None extends VerticalAlignment
    }

    def path(p: Path2D, stroke: BasicStroke, color: Color) =
      Path(p, stroke, color)

    def line(p1 : Point2D.Double, p2 : Point2D.Double, stroke : BasicStroke, color : Color) = 
      path({
        val p = new Path2D.Double
        p.moveTo(p1.getX, p1.getY)	
        p.lineTo(p2.getX, p2.getY)
        p
      }, stroke, color)
    
    def shape(s: java.awt.Shape, stroke: Option[(Stroke, Color)], fill: Option[Color]) =
      Shape(s, stroke, fill)

    def rectangle(width: Double, height: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) =
      shape(
        new Rectangle2D.Double(-width / 2, -height / 2, width, height),
        stroke,
        fill)

    def circle(diameter: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) =
      shape(
        new Ellipse2D.Double(-diameter / 2, -diameter / 2, diameter, diameter),
        stroke,
        fill)

    def label(text: String, font: Font, color: Color) =
      Label(text, font, color)

    def formulaLabel(formula: String, font: Font, color: Color) =
      formularendering.FormulaToGraphics(PodgonFontRenderContext).WithFont(font).print(formula).withColor(color)

    def compose(a: Expression[GraphicalContent], b: Expression[GraphicalContent]): Expression[GraphicalContent] =
      for (a <- a; b <- b) yield a.compose(b)

    def alignTransform(what: Rectangle2D, to: Rectangle2D, horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment): AffineTransform = {
      val xTranslate = horizontalAlignment match {
        case HorizontalAlignment.Left => to.getMinX - what.getMinX
        case HorizontalAlignment.Right => to.getMaxX - what.getMinX
        case HorizontalAlignment.Center => to.getMinX - what.getMinX + (to.getWidth - what.getWidth) / 2
        case HorizontalAlignment.None => 0
      }

      val yTranslate = verticalAlignment match {
        case VerticalAlignment.Top => to.getMinY - what.getMinY
        case VerticalAlignment.Bottom => to.getMaxY - what.getMinY
        case VerticalAlignment.Center => to.getMinY - what.getMinY + (to.getHeight - what.getHeight) / 2
        case VerticalAlignment.None => 0
      }

      AffineTransform.getTranslateInstance(xTranslate, yTranslate)
    }
  }
}
