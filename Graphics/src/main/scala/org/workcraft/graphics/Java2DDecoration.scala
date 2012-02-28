package org.workcraft.graphics
import java.awt.geom.Rectangle2D
import org.workcraft.graphics.formularendering.RichRectangle2D
import java.awt.font.GlyphVector
import java.awt.geom.Path2D
import java.awt.{Shape => ShapeJ}

object Java2DDecoration {
  implicit def decorateRectangle2D(rect : Rectangle2D.Double) = new RichRectangle2D(rect)
  private def doubulizeRect(rect : Rectangle2D) = new Rectangle2D.Double(rect.getMinX, rect.getMinY, rect.getWidth, rect.getHeight)
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
}
