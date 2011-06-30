package org.workcraft.plugins.cpog.scala

import org.workcraft.gui.Coloriser
import org.workcraft.dom.visual.DrawRequest
import org.workcraft.dom.visual.ColorisableGraphicalContent
import java.awt.Color
import java.awt.geom.Point2D
import java.awt.BasicStroke
import org.workcraft.dom.visual.Touchable
import org.workcraft.util.Maybe
import java.awt.Stroke

object Shape {
  def colorisableGraphicalContent(s: java.awt.Shape, stroke: Option[(Stroke, Color)], fillColor: Option[Color]) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.getGraphics()
      val colorisation = r.getColorisation().getColorisation()
      
      for (color <- fillColor) {
        g.setColor(Coloriser.colorise(color, colorisation))
        g.fill(s)
      }
      
      for ((stroke, color) <- stroke) {
        g.setStroke(stroke)
        g.setColor(Coloriser.colorise(color, colorisation))
        g.draw(s)
      }
    }
  }
  
  def richGraphicalContent (s: java.awt.Shape, stroke: Option[(Stroke, Color)], fillColor: Option[Color]) = 
  {
    val gc = colorisableGraphicalContent(s, stroke, fillColor)
    
    val outline = stroke match {
      case Some((stroke, _)) => stroke.createStrokedShape(s)
      case None => s
    }
    
    new RichGraphicalContent(gc, outline.getBounds2D, new Touchable {
      def hitTest(p:Point2D) = outline.contains(p.getX, p.getY) || s.contains(p.getX, p.getY)
      def getBoundingBox = outline.getBounds2D
      def getCenter = new Point2D.Double(0,0)
    })
  }
}