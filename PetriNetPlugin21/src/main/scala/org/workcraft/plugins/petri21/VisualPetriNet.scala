package org.workcraft.plugins.petri21
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.graphics.RichGraphicalContent
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz._
import org.workcraft.plugins.shared.CommonVisualSettings
import org.workcraft.graphics.Graphics._
import java.awt.BasicStroke
import org.workcraft.graphics.Graphics
import org.workcraft.graphics.LabelPositioning

object VisualPlace {
  def image (p: Place) : Expression[RichGraphicalContent] = 
    for (
        tokens <- p.tokens;
        label <- p.label;
        font <- CommonVisualSettings.serifFont;
        size <- CommonVisualSettings.size;
        strokeWidth <- CommonVisualSettings.strokeWidth;
        foreColor <- CommonVisualSettings.foregroundColor;
        fillColor <- CommonVisualSettings.fillColor;
        tokensImage <- TokenPainter.image(p.tokens)
    ) yield { 
      val placeImage = circle(size, Some((new BasicStroke (strokeWidth.toFloat), foreColor)), Some(fillColor))
      val labelImage = Graphics.label (label, font, foreColor)
      
      (labelImage alignSideways (placeImage, LabelPositioning.BOTTOM)) over
      (tokensImage align (placeImage, HorizontalAlignment.Center, VerticalAlignment.Center)) over
      tokensImage
    }
}

object VisualTransition {
  def image (t: Transition) : Expression[RichGraphicalContent] =
    for (
        label <- t.label;
        font <- CommonVisualSettings.serifFont;
        size <- CommonVisualSettings.size;
        strokeWidth <- CommonVisualSettings.strokeWidth;
        foreColor <- CommonVisualSettings.foregroundColor;
        fillColor <- CommonVisualSettings.fillColor
        ) yield {
      val transitionImage = rectangle (size, size, Some ((new BasicStroke(strokeWidth.toFloat), foreColor)), Some(fillColor))
      val labelImage = Graphics.label(label, font, foreColor)
      
      (labelImage alignSideways (transitionImage, LabelPositioning.BOTTOM)) over
      transitionImage
    }
}