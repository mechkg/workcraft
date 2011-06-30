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
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.Scalaz._
import org.workcraft.plugins.cpog.scala.Expressions._
import java.awt.geom.Path2D
import org.workcraft.dom.visual.Touchable
import org.workcraft.dom.visual.TransformHelper
import java.awt.geom.PathIterator
import org.workcraft.plugins.cpog.scala.touchable.TouchableUtil
import org.workcraft.plugins.cpog.scala.RichGraphicalContent
import org.workcraft.util.Maybe
import org.workcraft.gui.graph.tools.Colorisation

package org.workcraft.plugins.cpog.scala {
  object Graphics {
  
  implicit def asBCGC (x : RichGraphicalContent) = new BoundedColorisableGraphicalContent (x.colorisableGraphicalContent, x.visualBounds) 
  implicit def asTouchable (x : RichGraphicalContent) = x.touchable
    
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
      
      val minX = math.min (a.x, b.x)
      val minY = math.min (a.y, b.y)
      
      val maxX = math.max (a.x, b.x)
      val maxY = math.max (a.y, b.y)
      
      new Rectangle2D.Double (minX, minY, maxX-minX, maxY-minY)
    }

    def transform (graphics : BoundedColorisableGraphicalContent, transformation : AffineTransform) : BoundedColorisableGraphicalContent
     = new BoundedColorisableGraphicalContent ( transform (graphics.graphics, transformation), transform (graphics.boundingBox, transformation))
    
    def path (p: Path2D, stroke: BasicStroke, color : Color, touchThreshold : Double) = 
      Path.richGraphicalContent (p, stroke, color, touchThreshold)
    
   def shape (s: java.awt.Shape, stroke: Option[(Stroke, Color)], fill: Option[Color]) = 
     Shape.richGraphicalContent (s, stroke, fill)
    
    def rectangle(width: Double, height: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) =
      shape (
          new Rectangle2D.Double(-width / 2, -height / 2, width, height),
          stroke,
          fill
        )
        
    def circle(size: Double, stroke: Option[(Stroke, Color)], fill: Option[Color]) =
      shape(
          new Ellipse2D.Double(-size / 2, -size / 2, size, size),
          stroke,
          fill
        )
       
    def label (text : String, font : Font, color : Color) = 
      Label.richGraphicalContent (text, font, color)
        
    def formulaLabel (formula : String, font : Font, color : Color) =
      formularendering.FormulaToGraphics(VisualComponent.podgonFontRenderContext).WithFont(font).print(formula).asRichGraphicalContent(color)
      
    def compose (a : BoundedColorisableGraphicalContent, b : BoundedColorisableGraphicalContent) = BoundedColorisableGraphicalContent.compose (a,b)
    
    def compose (a : ColorisableGraphicalContent, b : ColorisableGraphicalContent) = org.workcraft.util.Graphics.compose (a,b)
    
    def compose (a : GraphicalContent, b : GraphicalContent) = org.workcraft.util.Graphics.compose (a,b)
    
    def compose (a: Expression[GraphicalContent], b : Expression[GraphicalContent]) : Expression[GraphicalContent] = 
      for (a <- a; b <- b) yield compose (a, b)
    
    def compose (list : List[RichGraphicalContent]) : RichGraphicalContent = list match {
      case Nil => RichGraphicalContent.empty
      case head :: Nil  => head
      case head :: tail => tail.foldRight(head)((a,b)=> a over b) 
    }
   
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