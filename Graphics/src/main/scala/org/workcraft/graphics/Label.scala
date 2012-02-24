package org.workcraft.graphics
import java.awt.Font
import java.awt.Color
import java.awt.geom.Point2D

object Label {
  def colorisableGraphicalContent(text: String, font: Font, color: Color) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.graphics
      g.setFont(font)

      g.setColor(Coloriser.colorise(color, r.colorisation.foreground))
      g.drawString(text, 0, 0)
    }
  }

  def visualBounds(text: String, font: Font) = font.createGlyphVector(PodgonFontRenderContext, text).getVisualBounds

  def touchable(text: String, font: Font) = Touchable.fromRect(font.createGlyphVector(PodgonFontRenderContext, text).getVisualBounds)

  def richGraphicalContent(text: String, font: Font, color: Color) =
    RichGraphicalContent (
        BoundedColorisableGraphicalContent (
            colorisableGraphicalContent(text, font, color), 
            PivotedBoundingBox(BoundingBox(visualBounds(text, font)), new Point2D.Double(0,0))
            ),
         touchable (text, font)            
        )
  
  def image(text: String, font: Font, color: Color) = richGraphicalContent(text, font, color)
}