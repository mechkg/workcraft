package org.workcraft.graphics
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.DrawRequest
import org.workcraft.gui.Coloriser
import java.awt.Font
import java.awt.Color
import org.workcraft.dom.visual.TouchableHelper
import org.workcraft.dom.visual.VisualComponent

object Label {
  def colorisableGraphicalContent (text : String, font : Font, color : Color) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.getGraphics
      g.setFont(font)
      g.setColor(Coloriser.colorise(color, r.getColorisation.getColorisation))
      g.drawString(text, 0, 0)
    }
  }
  
  def visualBounds (text : String, font : Font) = font.createGlyphVector(VisualComponent.podgonFontRenderContext, text).getVisualBounds
  
  def touchable (text : String, font : Font) = TouchableUtil.fromRectangle(font.createGlyphVector (VisualComponent.podgonFontRenderContext, text).getVisualBounds)
  
  def richGraphicalContent (text : String, font : Font, color : Color) = {
    val bb = visualBounds(text, font)
    new RichGraphicalContent(colorisableGraphicalContent(text, font, color), bb, TouchableUtil.fromRectangle(bb))
  }
}