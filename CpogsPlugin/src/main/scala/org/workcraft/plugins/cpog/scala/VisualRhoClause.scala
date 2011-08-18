import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.graphics.Graphics.HorizontalAlignment
import org.workcraft.graphics.Graphics.VerticalAlignment
import org.workcraft.graphics.Graphics.asBCGC
import org.workcraft.graphics.Graphics.rectangle
import org.workcraft.graphics.formularendering.FormulaToGraphics
import org.workcraft.graphics.RichGraphicalContent
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.expressions.Zero
import org.workcraft.plugins.cpog.scala.nodes.RhoClause
import org.workcraft.plugins.cpog.scala.nodes.Variable
import org.workcraft.plugins.shared.CommonVisualSettings
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz.maImplicit
import org.workcraft.graphics.formularendering.FormulaRenderingResult

package org.workcraft.plugins.cpog.scala {

  object VisualRhoClause {
    private val size: Double = 1
    private val strokeWidth: Float = 0.1f

    def image(rhoClause: RhoClause): Expression[RichGraphicalContent] =
      for (
        formula <- rhoClause.formula;
        value <- FormulaValue(formula);
        fillColor <- CommonVisualSettings.fillColor : Expression[Color];
        foreColor <- CommonVisualSettings.foregroundColor : Expression[Color];
        printedFormula <- (FormulaToGraphics.withPodgonFontRenderContext.withFancyFont.renderM[Expression, Variable](formula)(variable => variable.visualProperties.label) : Expression[FormulaRenderingResult])
      ) yield {
        val formulaColor = if (value == One.instance)
          new Color(0x00cc00)
        else if (value == Zero.instance)
          Color.RED
        else
          foreColor

        val formulaImage = printedFormula.asRichGraphicalContent(formulaColor)

        val frameImage = rectangle(
          formulaImage.boundingBox.getWidth + 0.4,
          formulaImage.boundingBox.getHeight + 0.4,
          Some((new BasicStroke(strokeWidth), foreColor)),
          Some(fillColor))

        (formulaImage align (frameImage, HorizontalAlignment.Center, VerticalAlignment.Center)) over frameImage
      }
  }
}