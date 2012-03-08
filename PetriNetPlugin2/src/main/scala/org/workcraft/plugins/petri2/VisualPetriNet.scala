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
import org.workcraft.graphics.Touchable
import java.awt.geom.Rectangle2D

object VisualPlace {
  def image (tokens: Expression[Int], label: Expression[String]) : Expression[BoundedColorisableGraphicalContent] = 
    for (
        t <- tokens;
        label <- label;
        size <- CommonVisualSettings.size;
        font <- CommonVisualSettings.labelFont;
        strokeWidth <- CommonVisualSettings.strokeWidth;
        foreColor <- CommonVisualSettings.foregroundColor;
        fillColor <- CommonVisualSettings.fillColor;
        tokensImage <- TokenPainter.image(tokens)
    ) yield {
      val place = circle(size, Some((new BasicStroke (strokeWidth.toFloat), foreColor)), Some(fillColor)).boundedColorisableGraphicalContent
      
      val placeWithTokens = tokensImage match {
        case Some(i) => place.compose(i)
        case _ => place
      }

      val labelImage = Graphics.label (label, font, foreColor).boundedColorisableGraphicalContent
      
      labelImage.alignSideways(placeWithTokens, LabelPositioning.Bottom).compose(placeWithTokens)
    }
  
  val touchable = CommonVisualSettings.size.map(size => Touchable.fromCircle(size/2))
}

object VisualTransition {
  def image (label: Expression[String]) : Expression[BoundedColorisableGraphicalContent] =
    for (
        label <- label;
        font <- CommonVisualSettings.labelFont;
        size <- CommonVisualSettings.size;
        strokeWidth <- CommonVisualSettings.strokeWidth;
        foreColor <- CommonVisualSettings.foregroundColor;
        fillColor <- CommonVisualSettings.fillColor
        ) yield {
      val transitionImage = rectangle (size, size, Some ((new BasicStroke(strokeWidth.toFloat), foreColor)), Some(fillColor)).boundedColorisableGraphicalContent
      val labelImage = Graphics.label(label, font, foreColor).boundedColorisableGraphicalContent
      
      (labelImage alignSideways (transitionImage, LabelPositioning.Bottom)).compose(transitionImage)
    }
  
  val touchable = CommonVisualSettings.size.map(size => Touchable.fromRect(new Rectangle2D.Double (-size/2, -size/2, size, size)))
}