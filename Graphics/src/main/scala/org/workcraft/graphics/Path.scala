package org.workcraft.graphics

import java.awt.Color
import java.awt.BasicStroke
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D

import Java2DDecoration._

class Path private (val p: Path2D, val stroke: BasicStroke, val color: Color, val touchThreshold: Double)

object Path {
  def apply(p: Path2D, stroke: BasicStroke, color: Color, touchThreshold: Double) = new Path (p, stroke, color, touchThreshold)
  
  implicit def graphicalContent(path: Path) = GraphicalContent( g => {
      g.setStroke(path.stroke)
      g.setColor(path.color)
      g.draw(path.p)
  })
  
  implicit def colorisableGraphicalContent(path: Path) = ColorisableGraphicalContent(colorisation => GraphicalContent ( g => {
      g.setStroke(path.stroke)
      g.setColor(Coloriser.colorise(path.color, colorisation.foreground))
      g.draw(path.p)
  }))
  
  implicit def boundedColorisableGraphicalContent(path: Path) = BoundedColorisableGraphicalContent (path, BoundingBox(path.p.bounds))
    
  implicit def touchable(path: Path) = new Touchable {
    val pathError = 0.01
    val segments = getSegments(path.p.getPathIterator(null, pathError))

    private def testSegments(segments: List[Line2D.Double], point: Point2D.Double, threshold: Double): Boolean = {
      val tSq = threshold * threshold
      for (s <- segments) {
        if (s.ptSegDistSq(point) < tSq)
          return true
      }
      false
    }

    private def getSegments(i: PathIterator): List[Line2D.Double] = {
      val coords = new Array[Double](6)

      var curX = 0.0
      var curY = 0.0

      var startX = 0.0
      var startY = 0.0

      var broken = true

      var segments: List[Line2D.Double] = Nil

      while (!i.isDone) {
        val t = i.currentSegment(coords)
        if (t == PathIterator.SEG_MOVETO) {
          curX = coords(0)
          curY = coords(1)
          broken = true
        } else if (t == PathIterator.SEG_LINETO) {
          segments = new Line2D.Double(curX, curY, coords(0), coords(1)) :: segments
          if (broken) {
            startX = curX
            startY = curY
            broken = false
          }
          curX = coords(0)
          curY = coords(1)
        } else if (t == PathIterator.SEG_CLOSE) {
          segments = new Line2D.Double(curX, curY, startX, startY) :: segments
          curX = startX
          curY = startY
          broken = true
        }

        i.next()
      }

      segments
    }

    def hitTest(point: Point2D.Double) = testSegments(segments, point, path.touchThreshold)
    def boundingBox = BoundingBox(path.p.bounds)
  }
}
