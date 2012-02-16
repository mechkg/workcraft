package org.workcraft.graphics

import java.awt.Color
import java.awt.geom.Point2D
import java.awt.BasicStroke
import java.awt.Stroke

object Shape {
  def colorisableGraphicalContent(s: java.awt.Shape, stroke: Option[(Stroke, Color)], fillColor: Option[Color]) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.graphics
      val colorisation = r.colorisation.foreground

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

  def richGraphicalContent(s: java.awt.Shape, stroke: Option[(Stroke, Color)], fillColor: Option[Color]) = {
    val outline = stroke match {
      case Some((stroke, _)) => stroke.createStrokedShape(s)
      case None => s
    }

    RichGraphicalContent(
      BoundedColorisableGraphicalContent(colorisableGraphicalContent(s, stroke, fillColor),
        PivotedBoundingBox(BoundingBox(outline.getBounds2D), new Point2D.Double(outline.getBounds2D.getCenterX, outline.getBounds2D.getCenterY))),
      new Touchable {
        def hitTest(p: Point2D) = outline.contains(p.getX, p.getY) || s.contains(p.getX, p.getY)
        def boundingBox = BoundingBox(outline.getBounds2D)
      })
  }
}