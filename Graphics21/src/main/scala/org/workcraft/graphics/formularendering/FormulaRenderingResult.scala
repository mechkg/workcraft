package org.workcraft.graphics.formularendering

import org.workcraft.gui.Coloriser
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.dom.visual.DrawRequest
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import java.awt.font.GlyphVector
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import org.workcraft.graphics.formularendering.RichRectangle2D._
import org.workcraft.graphics.TouchableUtil
import org.workcraft.graphics.Graphics._
import org.workcraft.graphics.RichGraphicalContent

case class FormulaRenderingResult(logicalBounds: Rectangle2D, visualBounds: Rectangle2D,
  glyphs: List[(GlyphVector, Point2D)], inversionLines: List[Line2D]) {

  def plus(summand: FormulaRenderingResult): FormulaRenderingResult = {
    val glyphs2 = glyphs ::: summand.glyphs.map({
      case (glyph, p) =>
        (glyph, new Point2D.Double(
          p.getX() + logicalBounds.getWidth(),
          p.getY()))
    });

    val inversionLines2 = inversionLines ::: summand.inversionLines.map(line =>
      new Line2D.Double(
        line.getX1() + logicalBounds.getWidth(), line.getY1(),
        line.getX2() + logicalBounds.getWidth(), line.getY2()));

    val theirLogicalBounds = summand.logicalBounds.offset (logicalBounds.getMaxX, 0)
    val theirVisualBounds  = summand.visualBounds.offset (logicalBounds.getMaxX, 0)

    FormulaRenderingResult(logicalBounds.createUnion(theirLogicalBounds), visualBounds.createUnion(theirVisualBounds), glyphs2, inversionLines2)
  }

  final def draw(g: Graphics2D, color: Color) {
    g.setColor(color);

    for ((glyph, pos) <- glyphs) {
      g.drawGlyphVector(glyph, pos.getX().toFloat, pos.getY().toFloat);
    }

    g.setStroke(new BasicStroke(0.025f));
    for (line <- inversionLines) g.draw(line);
  }

  def asRichGraphicalContent(color: Color) =
    {
      val gc = new ColorisableGraphicalContent() {
        override def draw(request: DrawRequest) {
          FormulaRenderingResult.this.draw(request.getGraphics(), Coloriser.colorise(color, request.getColorisation().getColorisation()));
        }
      }
      new RichGraphicalContent(gc, visualBounds, TouchableUtil.fromRectangle(visualBounds))
    }
}
object FormulaRenderingResult {
  val empty = FormulaRenderingResult(new Rectangle2D.Double(0, 0, 0, 0), new Rectangle2D.Double(0, 0, 0, 0), Nil, Nil)
}
