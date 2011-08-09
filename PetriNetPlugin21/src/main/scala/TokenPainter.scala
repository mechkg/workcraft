package org.workcraft.plugins.petri21

import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.graphics.RichGraphicalContent
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz._
import org.workcraft.plugins.shared.CommonVisualSettings
import org.workcraft.graphics.Graphics._

object TokenPainter {
  def image(tokens: Expression[Int]): Expression[RichGraphicalContent] =
    for (
      tokens <- tokens;
      size <- CommonVisualSettings.size;
      strokeWidth <- CommonVisualSettings.strokeWidth;
      foreColor <- CommonVisualSettings.foregroundColor;
      font <- CommonVisualSettings.serifFont;
      val singleTokenSize = size / 1.9;
      val multipleTokenSeparation = strokeWidth / 8
    ) yield if (tokens == 0)
      RichGraphicalContent.empty
    else if (tokens == 1)
      circle(singleTokenSize, None, Some(foreColor))
    else if (tokens > 1 && tokens < 8) {
      val radialTokens = Math.max(tokens, 6)
      val angularDistance = Math.Pi / radialTokens
      val R = (size / 2 - strokeWidth - multipleTokenSeparation) / (1 + Math.sin(angularDistance))
      val radiusTight = R * Math.sin(angularDistance)
      val radiusSparse = radiusTight - multipleTokenSeparation

      val tokenImage = circle(radiusSparse * 2, None, Some(foreColor))

      val radialTokensImage =
        compose(
          Range(0, radialTokens) map (i => tokenImage translate (-R * Math.sin(i * angularDistance * 2), -R * Math.cos(i * angularDistance * 2)))
          )

      if (tokens == 7)
        tokenImage over radialTokensImage
      else
        radialTokensImage
    } else
      label(tokens.toString, font.deriveFont((size / 2).toFloat), foreColor)
}