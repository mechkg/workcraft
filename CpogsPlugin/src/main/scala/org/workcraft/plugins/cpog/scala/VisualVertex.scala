import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.scala.Expressions._
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.graphics.Graphics._
import org.workcraft.dom.visual.DrawRequest
import java.awt.geom.Ellipse2D
import org.workcraft.plugins.shared.CommonVisualSettings
import org.workcraft.plugins.cpog.optimisation.expressions.Zero
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.gui.Coloriser
import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.Rectangle2D
import org.workcraft.graphics.formularendering._
import org.workcraft.graphics.LabelPositioning
import org.workcraft.dom.visual.Label
import org.workcraft.dependencymanager.advanced.core.Expressions.{ fmap => javafmap }
import pcollections.PVector
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import scalaz._
import org.workcraft.graphics.RichGraphicalContent

package org.workcraft.plugins.cpog.scala {
  object VisualVertex {
    val size: Double = 1
    private val strokeWidth: Float = 0.1f

    lazy val renderer = FormulaToGraphics.withPodgonFontRenderContext.withFancyFont 

    private def simpleLabel(label: String) = renderer.print(label)

    private def complexLabel(label: String, condition: BooleanFormula[Variable]) =
        for(img <- renderer.renderM[Expression, Variable](condition)((v:Variable) => v.visualProperties.label))
          yield simpleLabel(label) plus simpleLabel(": ") plus img 

    private def vertexGraphics(value: BooleanFormula[Variable], foreColor: Color, fillColor: Color) = {
      val effectiveForeColor = if (value == One.instance)
        foreColor
      else
        Color.LIGHT_GRAY

      val stroke = if (value == Zero.instance)
        new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array(0.18f, 0.18f), 0.0f)
      else
        new BasicStroke(strokeWidth)

      circle(size, Some((stroke, effectiveForeColor)), Some(fillColor))
    }
    
    private def labelGraphics(label: String, condition: BooleanFormula[Variable]) : Expression[RichGraphicalContent] =
      for(img <- if (condition == One.instance)
        implicitly[Monad[Expression]].pure(simpleLabel(label))
      else
        complexLabel(label, condition)) yield img.asRichGraphicalContent(Color.BLACK)

    def image(vertex: Vertex): Expression[RichGraphicalContent] =
      for(
      	label <- vertex.visualProperties.label;
      	condition <- vertex.condition;
      	value <- FormulaValue(condition);
      	labelPositioning <- vertex.visualProperties.labelPositioning;
      	fillColor <- CommonVisualSettings.fillColor;
      	foreColor <- CommonVisualSettings.foregroundColor;
      	labelGraphics <- labelGraphics(label, condition)
      ) yield {
        val vg = vertexGraphics(value, foreColor, fillColor)
        
        (labelGraphics alignSideways (vg, labelPositioning)) over (vg, vg.touchable)
      }
  }
}
