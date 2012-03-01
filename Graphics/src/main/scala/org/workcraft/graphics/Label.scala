package org.workcraft.graphics
import java.awt.Font
import java.awt.Color
import java.awt.geom.Point2D

import Java2DDecoration._

class Label private (val text: String, val font: Font, val color: Color) {
  def visualBounds = font.createGlyphVector(PodgonFontRenderContext, text).visualBounds
}

object Label {
  def apply(text: String, font: Font, color: Color) = new Label(text, font, color)
  
  implicit def graphicalContent(label: Label) = GraphicalContent(g => {
    g.setFont(label.font)
    g.setColor(label.color)
    g.drawString(label.text, 0, 0)
  })

  implicit def colorisableGraphicalContent(label: Label) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.graphics
      g.setFont(label.font)

      g.setColor(Coloriser.colorise(label.color, r.colorisation.foreground))
      g.drawString(label.text, 0, 0)
    }
  }

  implicit def boundedColorisableGraphicalContent(label: Label) = BoundedColorisableGraphicalContent(label, BoundingBox(label.visualBounds))

  implicit def touchable(label: Label) = Touchable.fromRect(label.font.createGlyphVector(PodgonFontRenderContext, label.text).visualBounds)
}
