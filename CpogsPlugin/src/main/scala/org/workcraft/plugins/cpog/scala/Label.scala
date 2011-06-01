package org.workcraft.plugins.cpog.scala
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.DrawRequest
import org.workcraft.gui.Coloriser
import java.awt.Font
import java.awt.Color
import org.workcraft.dom.visual.TouchableHelper
import org.workcraft.plugins.cpog.scala.touchable.TouchableUtil
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
  
  def touchable (text : String, font : Font) = TouchableUtil.fromRectangle(font.createGlyphVector (VisualComponent.podgonFontRenderContext, text).getVisualBounds)
}