package org.workcraft.graphics
import java.awt.Font
import java.awt.Color
import java.awt.geom.Point2D

class Label private (val text: String, val font: Font, val color: Color) {
  lazy val visualBounds = font.createGlyphVector(PodgonFontRenderContext, text).getVisualBounds
  
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
  
  lazy val boundedColorisableGraphicalContent = BoundedColorisableGraphicalContent(colorisableGraphicalContent, BoundingBox(visualBounds))
  
  lazy val touchable = Touchable.fromRect(visualBounds)
}

object Label {
  def apply(text: String, font: Font, color: Color) = new Label(text, font, color) 
}