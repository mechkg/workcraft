import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.dom.visual.DrawRequest
import java.awt.geom.Ellipse2D
import org.workcraft.plugins.shared.CommonVisualSettings
import org.workcraft.plugins.cpog.optimisation.expressions.Zero
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.gui.Coloriser
import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.Rectangle2D
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics
import org.workcraft.plugins.cpog.LabelPositioning
import org.workcraft.plugins.cpog.FormulaRenderer
import org.workcraft.dom.visual.Label
import org.workcraft.dependencymanager.advanced.core.Expressions.{fmap => javafmap}
import pcollections.PVector
import org.workcraft.gui.propertyeditor.EditableProperty

package org.workcraft.plugins.cpog.scala {
  object VisualVertex {
  val size : Double = 1
  val strokeWidth : Float = 1

  def image (vertex : Vertex) : Expression[BoundedColorisableGraphicalContent] = {
    val circle = new ExpressionBase[BoundedColorisableGraphicalContent] {
      override def evaluate(context:EvaluationContext) : BoundedColorisableGraphicalContent = {
        val gc = new ColorisableGraphicalContent {
          override def draw (r: DrawRequest) = {
            val g = r.getGraphics()
            val colorisation = r.getColorisation().getColorisation()
            val shape = new Ellipse2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
								size - strokeWidth, size - strokeWidth);
            
            val value = context.resolve (Vertex.conditionValue (vertex))
		    
			g.setColor(Coloriser.colorise(context.resolve(CommonVisualSettings.fillColor), colorisation))
			g.fill(shape)
						
			g.setColor(Coloriser.colorise(context.resolve(CommonVisualSettings.foregroundColor), colorisation));
			if (value == Zero.instance())
			{
				g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
				    BasicStroke.JOIN_MITER, 1.0f,  Array(0.18f, 0.18f), 0.0f));
			}
			else
			{
				g.setStroke(new BasicStroke(strokeWidth));
				if (value != One.instance())
					g.setColor(Coloriser.colorise(Color.LIGHT_GRAY, colorisation));
				}
						
			g.draw(shape);
          }
        }
					
		new BoundedColorisableGraphicalContent(gc, new Rectangle2D.Double(-size/2, -size/2, size, size));
      }
    }
      
      
      val nameLabel = new ExpressionBase[BoundedColorisableGraphicalContent] {
			override def evaluate(context : EvaluationContext) : BoundedColorisableGraphicalContent = {
				val text = context.resolve(vertex.visualProperties.label);
				val condition = context.resolve(vertex.condition);
				
				val finalText =	if (condition != One.instance()) text + ": " else text
				
				val result = FormulaToGraphics.print(text, FormulaRenderer.fancyFont, Label.podgonFontRenderContext());
				
				if (condition != One.instance()) result.add(FormulaToGraphics.render(condition, Label.podgonFontRenderContext(), FormulaRenderer.fancyFont));
				
				return LabelPositioning.positionRelative(context.resolve(circle).boundingBox
						, context.resolve(vertex.visualProperties.labelPositioning)
						, result.asBoundedColorisableImage());
			}
		}
   
      javafmap  (BoundedColorisableGraphicalContent.compose, circle, nameLabel)
  }
      
  //def getProperties (v : Vertex) : PVector[EditableProperty] = VisualComponent.getProperties (v.visualProperties)	
}
}