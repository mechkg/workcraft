import org.workcraft.plugins.shared.CommonVisualSettings


import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.plugins.cpog.scala.nodes.RhoClause
import org.workcraft.plugins.cpog.scala.Graphics._
import java.awt.geom.Rectangle2D
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.plugins.cpog.FormulaRenderer
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.expressions.Zero
import java.awt.Color
import java.awt.BasicStroke

package org.workcraft.plugins.cpog.scala {


object VisualRhoClause {
  private val size: Double = 1
  private val strokeWidth: Float = 0.1f
  
  def image (rhoClause : RhoClause) = new ExpressionBase[BoundedColorisableGraphicalContent] {
    override def evaluate (context : EvaluationContext) : BoundedColorisableGraphicalContent = {
      val formula = context.resolve(rhoClause.formula)
      val value = context.resolve  (Util.formulaValue (formula))
      val fillColor = context.resolve (CommonVisualSettings.fillColor)
      val foreColor = context.resolve (CommonVisualSettings.foregroundColor)
      
      
      val formulaColor = if (value == One.instance) 
    	  				   new Color(0x00cc00)
    	  				 else if (value == Zero.instance)
    	  				   Color.RED
    	  				 else
    	  				   foreColor
      
      val formulaImage = FormulaRenderer.render (formula).asBoundedColorisableImage(formulaColor)
      
      val frameImage = boundedRectangle (
            formulaImage.boundingBox.getWidth + 0.4,
            formulaImage.boundingBox.getHeight + 0.4,
            new BasicStroke (strokeWidth), 
            fillColor, 
            foreColor
              )
              
      compose (frameImage, 
          transform (formulaImage, 
                     alignTransform (formulaImage.boundingBox, 
                                     frameImage.boundingBox, 
                                     HorizontalAlignment.Center,
                                     VerticalAlignment.Center))
          )
    }
  }
}
}
