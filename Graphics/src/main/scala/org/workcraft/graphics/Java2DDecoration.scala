package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import org.workcraft.graphics.formularendering.RichRectangle2D
import java.awt.font.GlyphVector
import java.awt.geom.Path2D
import java.awt.{Shape => ShapeJ}
import java.awt.geom.Point2D
import java.awt.geom.Line2D
import java.awt.geom.AffineTransform

object Java2DDecoration {
  implicit def decorateRectangle2D(rect : Rectangle2D.Double) = new RichRectangle2D(rect)
  implicit def doubulizeRect(rect : Rectangle2D) = new Rectangle2D.Double(rect.getMinX, rect.getMinY, rect.getWidth, rect.getHeight)
  implicit def doubulizePt(pt : Point2D) = new Point2D.Double(pt.getX, pt.getY)
  implicit def decorateGlyphVector(glyph : GlyphVector) = new {
    import glyph._
    def visualBounds = doubulizeRect(getVisualBounds)
    def logicalBounds = doubulizeRect(getLogicalBounds)
  }
  implicit def decoratePath2D(path2d : Path2D) = new {
    import path2d._
    def bounds = doubulizeRect(getBounds2D)
  }
  implicit def decorateShape(shape : ShapeJ) = new {
    import shape._
    def bounds = doubulizeRect(getBounds2D)
  }
  implicit def decorateLine2D(line2D : Line2D) = new {
    import line2D._
    def p1 = doubulizePt(getP1)
    def p2 = doubulizePt(getP2)
  }
  implicit def decoratePoint2D(pt : Point2D) = new {
    def transform(t : AffineTransform) = {
      val p = new Point2D.Double
      t.transform(pt, p)
      p
    }
    def + (other: Point2D) = new Point2D.Double (pt.getX + other.getX, pt.getY + other.getY)
  }
}
