package org.workcraft.plugins.cpog.scala.formularendering

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
import org.workcraft.plugins.cpog.scala.formularendering.RichRectangle2D._

case class FormulaRenderingResult(boundingBox : Rectangle2D, 
      visualTop : Double, 
      glyphs : List[(GlyphVector, Point2D)], 
      inversionLines : List[Line2D]) {
  
	def plus(summand : FormulaRenderingResult) : FormulaRenderingResult = {
		val glyphs2 = glyphs ::: summand.glyphs.map({case (glyph, p) =>
			(glyph, new Point2D.Double(
					p.getX() + boundingBox.getWidth(), 
					p.getY()))});
		
		val inversionLines2 = inversionLines ::: summand.inversionLines.map(line => 
			new Line2D.Double(
					line.getX1() + boundingBox.getWidth(), line.getY1(),
					line.getX2() + boundingBox.getWidth(), line.getY2()));
		
		val bb2 = boundingBox.plus(new Point2D.Double(
				boundingBox.getMaxX() + summand.boundingBox.getWidth(), 
				summand.boundingBox.getMinY()))
		val visualTop2 = math.min(visualTop, summand.visualTop);
		
		FormulaRenderingResult(bb2, visualTop2, glyphs2, inversionLines2)
	}
	
	final def draw(g : Graphics2D, color : Color) {
		g.setColor(color);
		
		for((glyph, pos) <- glyphs) {
			g.drawGlyphVector(glyph, pos.getX().toFloat, pos.getY().toFloat);
		}
		
		g.setStroke(new BasicStroke(0.025f));
		for(line <- inversionLines) g.draw(line);		
	}
	
	def asBoundedColorisableImage(color : Color) = {
		new BoundedColorisableGraphicalContent(new ColorisableGraphicalContent() {
			override def draw(request : DrawRequest) {
				FormulaRenderingResult.this.draw(request.getGraphics(), Coloriser.colorise(color, request.getColorisation().getColorisation()));
			}
		}, boundingBox);
	}	
}
object FormulaRenderingResult {
  val empty = FormulaRenderingResult(new Rectangle2D.Double(0,0,0,0), 0, Nil, Nil)
}
