package org.workcraft.graphics.formularendering


import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import java.awt.font.GlyphVector
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.graphics.DrawRequest
import org.workcraft.graphics.GraphicalContent
import org.workcraft.graphics.Coloriser
import org.workcraft.graphics.PivotedBoundingBox
import org.workcraft.graphics.BoundingBox
import org.workcraft.graphics.Touchable

import org.workcraft.graphics.Java2DDecoration._

case class FormulaRenderingResult(logicalBounds: Rectangle2D.Double, visualBounds: Rectangle2D.Double,
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

    FormulaRenderingResult(logicalBounds.createUnionD(theirLogicalBounds), visualBounds.createUnionD(theirVisualBounds), glyphs2, inversionLines2)
  }

  final def draw(g: Graphics2D, color: Color) {
    g.setColor(color);

    for ((glyph, pos) <- glyphs) {
      g.drawGlyphVector(glyph, pos.getX().toFloat, pos.getY().toFloat);
    }

    g.setStroke(new BasicStroke(0.025f));
    for (line <- inversionLines) g.draw(line);
  }
  
  def withColor(color: Color) = FormulaImage (this, color)
}

case class FormulaImage private[formularendering] (rr: FormulaRenderingResult, color: Color)

object FormulaImage {
  implicit def graphicalContent (i: FormulaImage) = GraphicalContent (g => {i.rr.draw(g, i.color)})
  
  implicit def colorisableGraphicalContent (i: FormulaImage) = ColorisableGraphicalContent ( colorisation => (GraphicalContent(g => {
    i.rr.draw(g, Coloriser.colorise(i.color, colorisation.foreground))        
  })))
  
  implicit def boundedColorisableGraphicalContent (i: FormulaImage) = BoundedColorisableGraphicalContent (i, BoundingBox(i.rr.visualBounds))
  
  implicit def touchable (i: FormulaImage) = Touchable.fromRect(i.rr.visualBounds)
}


object FormulaRenderingResult {
  val empty = FormulaRenderingResult(new Rectangle2D.Double(0, 0, 0, 0), new Rectangle2D.Double(0, 0, 0, 0), Nil, Nil)
}