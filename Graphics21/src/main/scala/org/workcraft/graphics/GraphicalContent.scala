package org.workcraft.graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform

trait GraphicalContent {
  def draw(graphics: Graphics2D)
  
  def transform (transformation: AffineTransform) = new GraphicalContent {
        def draw (graphics: Graphics2D) {
           graphics.transform(transformation)
           GraphicalContent.this.draw(graphics)
        }
  }

  def compose(top: GraphicalContent) = new GraphicalContent {
    def draw(graphics: Graphics2D) = {
      val clonedGraphics = GraphicalContent.cloneGraphics(graphics)
      try {
        draw(clonedGraphics)
      } finally {
        clonedGraphics.dispose
      }
      top.draw(graphics)
    }

  }
}

object GraphicalContent {
  def cloneGraphics(g: Graphics2D) = g.create.asInstanceOf[Graphics2D]
  def apply(f: Graphics2D => Unit) = new GraphicalContent { def draw(graphics: Graphics2D) = f(graphics) }
  val Empty = apply(_ => {})
}
