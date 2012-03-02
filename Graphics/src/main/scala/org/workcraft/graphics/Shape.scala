package org.workcraft.graphics

import java.awt.Color
import java.awt.geom.Point2D
import java.awt.BasicStroke
import java.awt.Stroke

import Java2DDecoration._

class Shape private (val s: java.awt.Shape, val stroke: Option[(Stroke, Color)], val fillColor: Option[Color]) {
  lazy val graphicalContent = GraphicalContent ( g => {
      for (color <- fillColor) {
        g.setColor(color)
        g.fill(s)
      }

      for ((stroke, color) <- stroke) {
        g.setStroke(stroke)
        g.setColor(color)
        g.draw(s)
      }
  })
  
  lazy val colorisableGraphicalContent = new ColorisableGraphicalContent {
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
    
  lazy val boundedColorisableGraphicalContent = BoundedColorisableGraphicalContent (colorisableGraphicalContent, BoundingBox(s.bounds))
  
  lazy val touchable = new Touchable {
    def boundingBox = BoundingBox(s.bounds)
    def hitTest(point: Point2D.Double) = s.contains(point)
  }
}

object Shape {
  def apply (s: java.awt.Shape, stroke: Option[(Stroke, Color)], fillColor: Option[Color]) = new Shape(s, stroke, fillColor) 
}