package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import org.workcraft.graphics.formularendering.RichRectangle2D
import java.awt.font.GlyphVector
import java.awt.geom.Path2D
import java.awt.{ Shape => ShapeJ }
import java.awt.geom.Point2D
import java.awt.geom.Line2D
import java.awt.geom.AffineTransform
import java.awt.geom.CubicCurve2D

import Geometry.{lerp, createCubicCurve}

object Java2DDecoration {
  implicit def decorateRectangle2D(rect: Rectangle2D.Double) = new RichRectangle2D(rect)
  implicit def doubulizeRect(rect: Rectangle2D) = new Rectangle2D.Double(rect.getMinX, rect.getMinY, rect.getWidth, rect.getHeight)
  implicit def doubulizePt(pt: Point2D) = new Point2D.Double(pt.getX, pt.getY)
  implicit def decorateGlyphVector(glyph: GlyphVector) = new {
    import glyph._
    def visualBounds = doubulizeRect(getVisualBounds)
    def logicalBounds = doubulizeRect(getLogicalBounds)
  }
  implicit def decoratePath2D(path2d: Path2D) = new {
    import path2d._
    def bounds = doubulizeRect(getBounds2D)
  }
  implicit def decorateShape(shape: ShapeJ) = new {
    import shape._
    def bounds = doubulizeRect(getBounds2D)
  }
  implicit def decorateLine2D(line2D: Line2D) = new {
    import line2D._
    def p1 = doubulizePt(getP1)
    def p2 = doubulizePt(getP2)
  }

  implicit def decoratePoint2D(pt: Point2D) = new {
    def transform(t: AffineTransform) = {
      val p = new Point2D.Double
      t.transform(pt, p)
      p
    }
    def +(other: Point2D) = new Point2D.Double(pt.getX + other.getX, pt.getY + other.getY)
    def -(other: Point2D) = new Point2D.Double(pt.getX - other.getX, pt.getY - other.getY)
    def *(scale: Double) = new Point2D.Double(pt.getX * scale, pt.getY * scale)

    def dot(other: Point2D) = pt.getX() * other.getX() + pt.getY() * other.getY()

    def cross(other: Point2D) = pt.getX * other.getY - other.getX * pt.getY

    def rotate90CCW = new Point2D.Double(-pt.getY, pt.getX)
    
    def length = pt.distance(0, 0)

    def normalize = {
      val length = pt.distance(0, 0)
      if (length < 0.0000001)
        new Point2D.Double(0, 0)
      else
        new Point2D.Double(pt.getX / length, pt.getY / length)
    }
  }
  
  implicit def decorateCurve2D(curve: CubicCurve2D) = new {

    def getPointOnCurve(t: Double) = {
      val a1 = lerp(curve.getP1, curve.getCtrlP1, t)
      val a2 = lerp(curve.getCtrlP1, curve.getCtrlP2, t)
      val a3 = lerp(curve.getCtrlP2, curve.getP2, t)
      val b1 = lerp(a1, a2, t)
      val b2 = lerp(a2, a3, t)
      lerp(b1, b2, t)
    }

    def split(t: Double): (CubicCurve2D.Double, CubicCurve2D.Double) = {
      val a1 = lerp(curve.getP1, curve.getCtrlP1, t)
      val a2 = lerp(curve.getCtrlP1, curve.getCtrlP2, t)
      val a3 = lerp(curve.getCtrlP2, curve.getP2, t)
      val b1 = lerp(a1, a2, t)
      val b2 = lerp(a2, a3, t)
      val c = lerp(b1, b2, t)

      (createCubicCurve(curve.getP1, a1, b1, c), createCubicCurve(c, b2, a3, curve.getP2))
    }

    def getDerivative(t: Double): Point2D.Double = {
      val a1 = curve.getCtrlP1 - curve.getP1
      val a2 = curve.getCtrlP2 - curve.getCtrlP1
      val a3 = curve.getP2 - curve.getCtrlP2()
      val b1 = lerp(a1, a2, t)
      val b2 = lerp(a2, a3, t)

      lerp(b1, b2, t) * 3.0
    }

    def getSecondDerivative(t: Double): Point2D.Double = {
      val a1 = curve.getCtrlP1 - curve.getP1
      val a2 = curve.getCtrlP2 - curve.getCtrlP1
      val a3 = curve.getP2 - curve.getCtrlP2
      val b1 = a2 - a1
      val b2 = a3 - a2
      lerp(b1, b2, t) * 9.0
    }
  }

  def point (x: Double, y: Double) = new Point2D.Double(x, y)

  implicit def pointFromTuple (t: (Double, Double)) = new Point2D.Double (t._1, t._2)
}
