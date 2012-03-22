package org.workcraft.graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.Color
import org.workcraft.graphics.j.ColorisingGraphics2DStub
import scalaz.Semigroup
import scalaz.Scalaz._

trait GraphicalContent {
  def draw(graphics: Graphics2D)

  def transform(transformation: AffineTransform) = new GraphicalContent {
    def draw(graphics: Graphics2D) {
      graphics.transform(transformation)
      GraphicalContent.this.draw(graphics)
    }
  }

  def compose(top: GraphicalContent) = {
    val outer = this
    new GraphicalContent {
      def draw(graphics: Graphics2D) = {
        val clonedGraphics = GraphicalContent.cloneGraphics(graphics)
        try {
          outer.draw(clonedGraphics)
        } finally {
          clonedGraphics.dispose
        }
        top.draw(graphics)
      }
    }
  }

  def deriveColorisable = new ColorisableGraphicalContent {
    def draw(r: DrawRequest) = {
      GraphicalContent.this.draw(new ColorisingGraphics2DStub(r.graphics) {
        def setColor(c: Color) = r.graphics.setColor(Coloriser.colorise(c, r.colorisation.foreground))
        def setBackground(c: Color) = r.graphics.setBackground(Coloriser.colorise(c, r.colorisation.background))
      })
    }
  }
}

object GraphicalContent {
  def cloneGraphics(g: Graphics2D) = g.create.asInstanceOf[Graphics2D]
  def apply(f: Graphics2D => Unit) = new GraphicalContent { def draw(graphics: Graphics2D) = f(graphics) }
  val Empty = apply(_ => {})
  implicit def gcSemigroup : Semigroup[GraphicalContent] = semigroup((a,b) => a.compose(b))
}
