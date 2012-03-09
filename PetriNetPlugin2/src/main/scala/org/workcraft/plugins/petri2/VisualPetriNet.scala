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
  def image (tokens: Expression[Int], label: Expression[String], settings: CommonVisualSettings) : Expression[BoundedColorisableGraphicalContent] =
    
    (label <**> TokenPainter.image(tokens, settings))( (label, tokensImage) => {
      val place = circle(settings.size, Some((new BasicStroke (settings.strokeWidth.toFloat), settings.foregroundColor)), Some(settings.fillColor)).boundedColorisableGraphicalContent
      
      val placeWithTokens = tokensImage match {
        case Some(i) => place.compose(i)
        case _ => place
      }

      val labelImage = Graphics.label (label, settings.effectiveLabelFont, settings.foregroundColor).boundedColorisableGraphicalContent
      
      labelImage.alignSideways(placeWithTokens, LabelPositioning.Bottom).compose(placeWithTokens)
    })
  
  val touchable = CommonVisualSettings.settings.map(settings => Touchable.fromCircle(settings.size/2))
}

object VisualTransition {
  def image (label: Expression[String], settings: CommonVisualSettings) : Expression[BoundedColorisableGraphicalContent] =
    label.map{label =>
      val transitionImage = rectangle (settings.size, settings.size, Some ((new BasicStroke(settings.strokeWidth.toFloat), settings.foregroundColor)), Some(settings.fillColor)).boundedColorisableGraphicalContent
      val labelImage = Graphics.label(label, settings.labelFont, settings.foregroundColor).boundedColorisableGraphicalContent
      
      (labelImage alignSideways (transitionImage, LabelPositioning.Bottom)).compose(transitionImage)
    }
  
  val touchable = CommonVisualSettings.settings.map(settings => Touchable.fromRect(new Rectangle2D.Double (-settings.size/2, -settings.size/2, settings.size, settings.size)))
}
