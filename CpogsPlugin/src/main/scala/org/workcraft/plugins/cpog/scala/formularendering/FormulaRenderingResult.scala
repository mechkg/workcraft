package org.workcraft.plugins.cpog.scala.formularendering

import java.awt.geom.Rectangle2D
import java.awt.font.GlyphVector
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.scala.formularendering.RichRectangle2D._

class FormulaRenderingResult(boundingBox : Rectangle2D, 
      visualTop : Double, 
      glyphs : List[GlyphVector], 
      glyphCoordinates : List[Point2D], 
      inversionLines : List[Line2D]) {
  
	def plus(summand : FormulaRenderingResult) : FormulaRenderingResult = {
		val glyphs2 = glyphs ::: summand.glyphs
		
		val glyphCoordinates = glyphCoordinates ::: summand.glyphCoordinates.map(p =>
			new Point2D.Double(
					p.getX() + boundingBox.getWidth(), 
					p.getY()));
		
		val inversionLines2 = inversionLines ::: summand.inversionLines.map(line => 
			new Line2D.Double(
					line.getX1() + boundingBox.getWidth(), line.getY1(),
					line.getX2() + boundingBox.getWidth(), line.getY2()));
		
		val bb2 = boundingBox.plus(new Point2D.Double(
				boundingBox.getMaxX() + summand.boundingBox.getWidth(), 
				summand.boundingBox.getMinY()))
		val visualTop2 = Math.min(visualTop, summand.visualTop);
		
		FormulaRenderingResult(bb2, visualTop2, glyphs2, glyphCoordinates2, inversionLines2)
	}
}
