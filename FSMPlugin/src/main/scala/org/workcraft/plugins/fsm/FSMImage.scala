package org.workcraft.plugins.fsm
import org.workcraft.gui.CommonVisualSettings

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
import java.awt.Color
import org.workcraft.graphics.VisualCurveProperties

object FSMImage {
  def stateImage(label: Expression[String], initial: Expression[Boolean], settings: CommonVisualSettings): Expression[BoundedColorisableGraphicalContent] =

    (label <**> initial) {
      case (label, initial) => {
        val img = circle(settings.size, Some((new BasicStroke(settings.strokeWidth.toFloat), settings.foregroundColor)), Some(settings.fillColor)).boundedColorisableGraphicalContent

        val stateImg = if (initial) img.compose(
          circle(settings.size - settings.strokeWidth * 3, Some((new BasicStroke(settings.strokeWidth.toFloat / 2.0f), settings.foregroundColor)), Some(settings.fillColor)).boundedColorisableGraphicalContent)
        else img

        val labelImage = Graphics.label(label, settings.effectiveLabelFont, settings.foregroundColor).boundedColorisableGraphicalContent

        stateImg.compose(labelImage.align(stateImg, HorizontalAlignment.Center, VerticalAlignment.Center))
      }
    }

  val stateTouchable = CommonVisualSettings.settings.map(settings => Touchable.fromCircle(settings.size / 2))
}