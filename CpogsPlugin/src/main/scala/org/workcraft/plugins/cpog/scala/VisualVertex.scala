import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.Graphics._
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
import org.workcraft.dependencymanager.advanced.core.Expressions.{ fmap => javafmap }
import pcollections.PVector
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.plugins.cpog.optimisation.BooleanFormula

package org.workcraft.plugins.cpog.scala {
  object VisualVertex {
    val size: Double = 1
    private val strokeWidth: Float = 0.1f

    private def simpleLabel(label: String) = FormulaToGraphics.print(label, FormulaRenderer.fancyFont, Label.podgonFontRenderContext())

    private def complexLabel(label: String, condition: BooleanFormula) =
      simpleLabel(label + ": ") add
        FormulaToGraphics.render(condition, Label.podgonFontRenderContext(), FormulaRenderer.fancyFont)

    private def vertexGraphics(value: BooleanFormula, foreColor: Color, fillColor: Color) = {
      val effectiveForeColor = if (value == One.instance())
        foreColor
      else
        Color.LIGHT_GRAY

      val stroke = if (value == Zero.instance())
        new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
      else
        new BasicStroke(strokeWidth)

      boundedCircle(size, stroke, fillColor, effectiveForeColor)
    }

    private def labelGraphics(label: String, condition: BooleanFormula) =
      (if (condition == One.instance)
        simpleLabel(label)
      else
        complexLabel(label, condition)).asBoundedColorisableImage

    def image(vertex: Vertex): Expression[BoundedColorisableGraphicalContent] = new ExpressionBase[BoundedColorisableGraphicalContent] {
      override def evaluate(context: EvaluationContext): BoundedColorisableGraphicalContent = {

        val value = context.resolve(Util.formulaValue(context.resolve(vertex.condition)))
        val label = context.resolve(vertex.visualProperties.label);
        val condition = context.resolve(vertex.condition);
        val labelPositioning = context.resolve(vertex.visualProperties.labelPositioning)
        val fillColor = context.resolve(CommonVisualSettings.fillColor)
        val foreColor = context.resolve(CommonVisualSettings.foregroundColor)

        val vg = vertexGraphics(value, foreColor, fillColor)
        val lg = LabelPositioning.positionRelative(vg.boundingBox, labelPositioning, labelGraphics(label, condition))

        BoundedColorisableGraphicalContent.compose(vg, lg)
      }
    }

    //def getProperties (v : Vertex) : PVector[EditableProperty] = VisualComponent.getProperties (v.visualProperties)
  }
}
					
     
	
