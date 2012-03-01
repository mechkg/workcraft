package org.workcraft.graphics

import java.awt.Color
import java.awt.geom.Point2D
import java.awt.BasicStroke
import java.awt.Stroke

import Java2DDecoration._

class Shape private (val s: java.awt.Shape, val stroke: Option[(Stroke, Color)], val fillColor: Option[Color])

object Shape {
  def apply (s: java.awt.Shape, stroke: Option[(Stroke, Color)], fillColor: Option[Color]) = new Shape(s, stroke, fillColor)
  
  implicit def graphicalContent(shape: Shape) = GraphicalContent ( g => {
      for (color <- shape.fillColor) {
        g.setColor(color)
        g.fill(shape.s)
      }

      for ((stroke, color) <- shape.stroke) {
        g.setStroke(stroke)
        g.setColor(color)
        g.draw(shape.s)
      }
  })
  
  implicit def colorisableGraphicalContent(shape: Shape) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.graphics
      val colorisation = r.colorisation.foreground

      for (color <- shape.fillColor) {
        g.setColor(Coloriser.colorise(color, colorisation))
        g.fill(shape.s)
      }

      for ((stroke, color) <- shape.stroke) {
        g.setStroke(stroke)
        g.setColor(Coloriser.colorise(color, colorisation))
        g.draw(shape.s)
      }
    }
  }
  
  implicit def boundedColorisableGraphicalContent(shape: Shape) = BoundedColorisableGraphicalContent (shape, BoundingBox(shape.s.bounds))
      
  implicit def touchable(shape: Shape) = new Touchable {
    def boundingBox = BoundingBox(shape.s.bounds)
    def hitTest(point: Point2D.Double) = shape.s.contains(point)
  }
}