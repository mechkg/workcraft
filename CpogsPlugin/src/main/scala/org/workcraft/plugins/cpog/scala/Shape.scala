package org.workcraft.plugins.cpog.scala

import org.workcraft.gui.Coloriser
import org.workcraft.dom.visual.DrawRequest
import org.workcraft.dom.visual.ColorisableGraphicalContent
import java.awt.Color
import java.awt.geom.Point2D
import java.awt.BasicStroke
import org.workcraft.dom.visual.Touchable

object Shape {
  def colorisableGraphicalContent(s: java.awt.Shape, stroke: BasicStroke, fillColor: Color, foregroundColor: Color) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.getGraphics();
      val colorisation = r.getColorisation().getColorisation();

      g.setStroke(stroke);

      g.setColor(Coloriser.colorise(fillColor, colorisation));
      g.fill(s);

      g.setColor(Coloriser.colorise(foregroundColor, colorisation));
      g.draw(s);
    }
  }

  def touchable(s: java.awt.Shape) = new Touchable {
    override def hitTest(point: Point2D) = s.contains(point)
    override def getBoundingBox = s.getBounds
    override def getCenter = new Point2D.Double(0, 0)
  }
}