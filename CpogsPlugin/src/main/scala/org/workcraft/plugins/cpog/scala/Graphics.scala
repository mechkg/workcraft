import org.workcraft.dom.visual.DrawRequest

import org.workcraft.gui.Coloriser

import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.Stroke
import java.awt.Color

import java.awt.BasicStroke
import java.awt.Shape
import java.awt.Font
import org.workcraft.gui.Coloriser
import java.awt.geom.AffineTransform

import java.awt.geom.Point2D
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dom.visual.VisualComponent
import org.workcraft.plugins.cpog.scala.formularendering.FormulaToGraphics

import org.workcraft.plugins.cpog.LabelPositioning
import org.workcraft.plugins.cpog.scala.Expressions.monadicSyntax

package org.workcraft.plugins.cpog.scala {
  
import org.workcraft.plugins.cpog.formularendering.FancyPrinter
    
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
        def transform (graphics : ColorisableGraphicalContent, transformation : AffineTransform) : ColorisableGraphicalContent =
      new ColorisableGraphicalContent {
        def draw (r : DrawRequest) {
           r.getGraphics().transform(transformation)
           graphics.draw(r)
        }
    }
    
    def transform (bb : Rectangle2D, transformation : AffineTransform) : Rectangle2D = 
    {
      val a = new Point2D.Double(bb.getMinX, bb.getMinY)
      val b = new Point2D.Double(bb.getMaxX, bb.getMaxY)
      transformation.transform(a,a)
      transformation.transform(b,b)
      
      val minX = Math.min (a.x, b.x)
      val minY = Math.min (a.y, b.y)
      
      val maxX = Math.max (a.x, b.x)
      val maxY = Math.max (a.y, b.y)
      
      new Rectangle2D.Double (minX, minY, maxX-minX, maxY-minY)
    }

    def transform (graphics : BoundedColorisableGraphicalContent, transformation : AffineTransform) : BoundedColorisableGraphicalContent
     = new BoundedColorisableGraphicalContent ( transform (graphics.graphics, transformation), transform (graphics.boundingBox, transformation)) 
    
    
    def shape(s: Shape, stroke: BasicStroke, fillColor: Color, foregroundColor: Color) = new ColorisableGraphicalContent {
      override def draw(r: DrawRequest) = {
        val g = r.getGraphics();
        val colorisation = r.getColorisation().getColorisation();

        g.setStroke(stroke);

        g.setColor(Coloriser.colorise(fillColor, colorisation));
        g.fill(s);

        g.setColor(Coloriser.colorise(foregroundColor, colorisation));
        g.draw(s);
      }
    }

    def rectangle(width: Double, height: Double, stroke: BasicStroke, fillColor: Color, foregroundColor: Color) =
      shape(
        new Rectangle2D.Double(-width / 2, -height / 2, width, height),
        stroke,
        fillColor,
        foregroundColor
        )

    def boundedRectangle(width: Double, height: Double, stroke: BasicStroke, fillColor: Color, foregroundColor: Color) =
      new BoundedColorisableGraphicalContent(
        rectangle(width, height, stroke, fillColor, foregroundColor),
        new Rectangle2D.Double(-width / 2, -height / 2, width, height)
        )

    def circle(size: Double, stroke: BasicStroke, fillColor: Color, foregroundColor: Color) =
      shape(
        {
          val strokeWidth = stroke.getLineWidth
          new Ellipse2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2, size - strokeWidth,
            size - strokeWidth)
        },
        stroke,
        fillColor,
        foregroundColor
        )
        
    def boundedCircle(size: Double, stroke: BasicStroke, fillColor: Color, foregroundColor: Color) = 
      new BoundedColorisableGraphicalContent(
        circle(size, stroke, fillColor, foregroundColor),
        new Rectangle2D.Double(-size / 2, -size / 2, size, size)
        )
    
    def label (text : String, font : Font, color : Color) = new ColorisableGraphicalContent {
      override def draw (r : DrawRequest) = {
		val g = r.getGraphics
        g.setFont(font)
        g.setColor(color)
		g.drawString(text, 0, 0)
      }
    }
    
    def boundedLabel(text : String, font : Font, color : Color) = 
      new BoundedColorisableGraphicalContent(
          label (text, font, color),
          font.createGlyphVector (VisualComponent.podgonFontRenderContext, text).getVisualBounds
          )
    
    def boundedFormulaLabel (formula : String, font : Font, color : Color) = 
      FancyPrinter.print(formula, font, VisualComponent.podgonFontRenderContext).asBoundedColorisableImage(color)
      
    def compose (a : BoundedColorisableGraphicalContent, b : BoundedColorisableGraphicalContent) = BoundedColorisableGraphicalContent.compose (a,b)
    
    val compose = (a : GraphicalContent, b : GraphicalContent) => org.workcraft.util.Graphics.compose (a,b)
    
    def compose (list : List[BoundedColorisableGraphicalContent]) : BoundedColorisableGraphicalContent = list match {
      case Nil => BoundedColorisableGraphicalContent.EMPTY
      case head :: Nil  => head
      case head :: tail => tail.foldRight(head)((a,b)=>BoundedColorisableGraphicalContent.compose(a,b)) 
    }
    
    def aligned (what: BoundedColorisableGraphicalContent, to : BoundedColorisableGraphicalContent, 
        horizontalAlignment : HorizontalAlignment, verticalAlignment : VerticalAlignment) =
      transform (what, alignTransform(what.boundingBox, to.boundingBox, horizontalAlignment, verticalAlignment))
      
    def sideways (what: BoundedColorisableGraphicalContent, relativeTo : BoundedColorisableGraphicalContent, position : LabelPositioning) =
      LabelPositioning.positionRelative (relativeTo.boundingBox, position, what)
    
    def alignTransform (what : Rectangle2D, to : Rectangle2D, horizontalAlignment : HorizontalAlignment, verticalAlignment : VerticalAlignment) : AffineTransform  = {
      val xTranslate = horizontalAlignment match {
        case HorizontalAlignment.Left   => to.getMinX - what.getMinX
        case HorizontalAlignment.Right  => to.getMaxX - what.getMinX
        case HorizontalAlignment.Center => to.getMinX - what.getMinX + (to.getWidth - what.getWidth) / 2
        case HorizontalAlignment.None   => 0
      }
      
      val yTranslate = verticalAlignment match {
        case VerticalAlignment.Top    => to.getMinY - what.getMinY
        case VerticalAlignment.Bottom => to.getMaxY - what.getMinY
        case VerticalAlignment.Center => to.getMinY - what.getMinY + (to.getHeight - what.getHeight) / 2
        case VerticalAlignment.None   => 0
      }
      
      AffineTransform.getTranslateInstance(xTranslate, yTranslate)
    }
      
  }
}
