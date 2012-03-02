package org.workcraft.plugins.petri2

import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz._
import org.workcraft.gui.CommonVisualSettings
import org.workcraft.graphics.Graphics._
import java.awt.Color
import java.awt.Font
import java.awt.geom.Point2D
import org.workcraft.graphics.BoundedColorisableGraphicalContent

object TokenPainter {
  def image(tokens: Expression[Int]): Expression[Option[BoundedColorisableGraphicalContent]] =
    for (
      tokens <- tokens;
      size <- CommonVisualSettings.size;
      strokeWidth <- CommonVisualSettings.strokeWidth;
      foreColor <- CommonVisualSettings.foregroundColor;
      font <- CommonVisualSettings.serifFont;
      val singleTokenSize = size / 1.9;
      val multipleTokenSeparation = strokeWidth / 8
    ) yield if (tokens == 0)
      None
    else if (tokens == 1)
      Some(circle(singleTokenSize, None, Some(foreColor)).boundedColorisableGraphicalContent)
    else if (tokens > 1 && tokens < 8) {
      val radialTokens = Math.min(tokens, 6)
      val angularDistance = Math.Pi / radialTokens
      val R = (size / 2 - strokeWidth - multipleTokenSeparation) / (1 + Math.sin(angularDistance))
      val radiusTight = R * Math.sin(angularDistance)
      val radiusSparse = radiusTight - multipleTokenSeparation

      val tokenImage = circle(radiusSparse * 2, None, Some(foreColor)).boundedColorisableGraphicalContent

      val radialTokensImage =
          Range(0, radialTokens).map (i => tokenImage translate (-R * Math.sin(i * angularDistance * 2), -R * Math.cos(i * angularDistance * 2))).reduceLeft(_.compose(_))

      if (tokens == 7)
        Some(radialTokensImage.compose(tokenImage))
      else
        Some(radialTokensImage)
    } else
      Some(label(tokens.toString, font.deriveFont((size / 2).toFloat), foreColor).boundedColorisableGraphicalContent)
}