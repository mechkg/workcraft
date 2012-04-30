package org.workcraft.graphics
import java.awt.Font
import java.awt.Color
import java.awt.geom.Point2D

import Java2DDecoration._

class Label private (val text: String, val font: Font, val color: Color) {
  lazy val visualBounds = font.createGlyphVector(PodgonFontRenderContext, text).visualBounds
  lazy val logicalBounds = font.createGlyphVector(PodgonFontRenderContext, text).logicalBounds

  lazy val graphicalContent = GraphicalContent(g => {
    g.setFont(font)
    g.setColor(color)
    g.drawString(text, 0, 0)
  })
  
  lazy val colorisableGraphicalContent = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.graphics
      g.setFont(font)

      g.setColor(Coloriser.colorise(color, r.colorisation.foreground))
      g.drawString(text, 0, 0)
    }
  }
  
  lazy val boundedColorisableGraphicalContent = BoundedColorisableGraphicalContent(colorisableGraphicalContent, BoundingBox(visualBounds, logicalBounds))
  
  lazy val touchable = Touchable.fromRect(logicalBounds)
}

object Label {
  def apply(text: String, font: Font, color: Color) = new Label(text, font, color) 
}
