package org.workcraft.plugins.petri2

import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._
import org.workcraft.graphics.Graphics._
import java.awt.BasicStroke
import org.workcraft.graphics.Graphics
import org.workcraft.graphics.LabelPositioning
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.gui.CommonVisualSettings

object VisualPlace {
  implicit def image (p: Place) : Expression[BoundedColorisableGraphicalContent] = 
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
      val place = circle(size, Some((new BasicStroke (strokeWidth.toFloat), foreColor)), Some(fillColor)).boundedColorisableGraphicalContent
      
      val placeWithTokens = tokensImage match {
        case Some(i) => place.compose(i)
        case _ => place
      }

      val labelImage = Graphics.label (label, font, foreColor).boundedColorisableGraphicalContent
      
      labelImage.alignSideways(placeWithTokens, LabelPositioning.Bottom).compose(placeWithTokens)
    }
}

object VisualTransition {
  def image (t: Transition) : Expression[BoundedColorisableGraphicalContent] =
    for (
        label <- t.label;
        font <- CommonVisualSettings.serifFont;
        size <- CommonVisualSettings.size;
        strokeWidth <- CommonVisualSettings.strokeWidth;
        foreColor <- CommonVisualSettings.foregroundColor;
        fillColor <- CommonVisualSettings.fillColor
        ) yield {
      val transitionImage = rectangle (size, size, Some ((new BasicStroke(strokeWidth.toFloat), foreColor)), Some(fillColor)).boundedColorisableGraphicalContent
      val labelImage = Graphics.label(label, font, foreColor).boundedColorisableGraphicalContent
      
      (labelImage alignSideways (transitionImage, LabelPositioning.Bottom)).compose(transitionImage)
    }
}