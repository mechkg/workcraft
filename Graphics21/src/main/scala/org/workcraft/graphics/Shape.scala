package org.workcraft.graphics

import org.workcraft.gui.Coloriser
import org.workcraft.dom.visual.DrawRequest
import org.workcraft.dom.visual.ColorisableGraphicalContent
import java.awt.Color
import java.awt.geom.Point2D
import java.awt.BasicStroke
import org.workcraft.dom.visual.Touchable
import org.workcraft.util.Maybe
import java.awt.Stroke
import java.awt.geom.Rectangle2D

import org.workcraft.graphics.Java2DDecoration._

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
    
    new RichGraphicalContent(gc, outline.bounds, new Touchable {
      def hitTest(p:Point2D.Double) = outline.contains(p.getX, p.getY) || s.contains(p.getX, p.getY)
      def getBoundingBox = outline.bounds
      def getCenter = new Point2D.Double(0,0)
    })
  }
}
