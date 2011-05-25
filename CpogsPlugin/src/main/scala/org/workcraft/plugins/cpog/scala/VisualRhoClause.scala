import org.workcraft.plugins.shared.CommonVisualSettings

import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.plugins.cpog.scala.nodes.RhoClause
import org.workcraft.plugins.cpog.scala.Expressions._
import org.workcraft.plugins.cpog.scala.Scalaz._
import org.workcraft.plugins.cpog.scala.Graphics._
import org.workcraft.plugins.cpog.scala.formularendering.FormulaToGraphics
import java.awt.geom.Rectangle2D
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.expressions.Zero
import java.awt.Color
import java.awt.BasicStroke

package org.workcraft.plugins.cpog.scala {

  object VisualRhoClause {
    private val size: Double = 1
    private val strokeWidth: Float = 0.1f

    def image(rhoClause: RhoClause) =
      for (
        formula <- rhoClause.formula;
        value <- Util.formulaValue(formula);
        fillColor <- CommonVisualSettings.fillColor;
        foreColor <- CommonVisualSettings.foregroundColor;
        printedFormula <- FormulaToGraphics.render(formula)
      ) yield {
        val formulaColor = if (value == One.instance)
          new Color(0x00cc00)
        else if (value == Zero.instance)
          Color.RED
        else
          foreColor

		val formulaImage = printedFormula.asBoundedColorisableImage(formulaColor)
		
		val frameImage = boundedRectangle(
		    formulaImage.boundingBox.getWidth + 0.4,
		    formulaImage.boundingBox.getHeight + 0.4,
		    new BasicStroke(strokeWidth),
		    fillColor,
		    foreColor)
		
		compose(frameImage,
				  aligned(formulaImage, frameImage, HorizontalAlignment.Center, VerticalAlignment.Center)) : BoundedColorisableGraphicalContent
      }
  }
}
