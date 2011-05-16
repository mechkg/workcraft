package org.workcraft.plugins.cpog.scala.formularendering

import java.awt.font.FontRenderContext
import java.awt.Font
import java.awt.geom.Rectangle2D
import java.awt.geom.Line2D
import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions._
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import RichRectangle2D._

object FormulaToGraphics {
	def print[Var](formula : BooleanFormula[Var], font : Font, fontRenderContext : FontRenderContext, varPrinter : Var => String) : FormulaRenderingResult = {
	  class DefaultPrinter(p : BooleanFormula[Var] => FormulaRenderingResult) extends BooleanVisitor[Var, FormulaRenderingResult] {
	    def visit(v : Variable[Var]) = p(v)
	    def visit(v : Iff[Var]) = p(v)
	    def visit(v : Or[Var]) = p(v)
	    def visit(v : And[Var]) = p(v)
	    def visit(v : Xor[Var]) = p(v)
	    def visit(v : Not[Var]) = p(v)
	  }
	  def printIff(x : BooleanFormula[Var]) : FormulaRenderingResult = (new DefaultPrinter(printImply(_)) {
	    def visit(v : Iff[Var]) = printBinary(printIff(_), " = ", v) 
	  }) (x);
	  def printImply(x : BooleanFormula[Var]) : FormulaRenderingResult = (new DefaultPrinter(printOr(_)) {
	    def visit(v : Imply[Var]) = printBinary(printOr(_), " => ", v)
	  }) (x)
	  def printOr(x : BooleanFormula[Var]) : FormulaRenderingResult = (new DefaultPrinter(printXor(_)) {
	    def visit(v : Or[Var]) = printBinary(printOr(_), " + ", v)
	  }) (x)
	  def printXor(x : BooleanFormula[Var]) : FormulaRenderingResult = (new DefaultPrinter(printAnd(_)) {
	    def visit(v : Xor[Var]) = printBinary(printXor(_), " ^ ", v)
	  }) (x)
	  def printAnd(x : BooleanFormula[Var]) : FormulaRenderingResult = (new DefaultPrinter(printNot(_)) {
	    def visit(v : And[Var]) = printBinary(printAnd(_), " ^ ", v)
	  }) (x)
	  def printBinary(printer : BooleanFormula[Var] => FormulaRenderingResult, symbol : String, formula : BinaryBooleanFormula[Var]) = {
	    null
	  }
	  
	  def printNot(x : BooleanFormula[Var]) : FormulaRenderingResult = (new DefaultPrinter(printNot(_)) {
	    def visit(node : Not[Var]) =
			printIff(node.getX()) match {
			  case FormulaRenderingResult(boundingBox, visualTop, glyphs, glyphCoordinates, inversionLines) => {
			    val inversionLines2 = new Line2D.Double(
			    		boundingBox.getMinX(), visualTop,
			    		boundingBox.getMaxX(), visualTop)
			    val boundingBox2 = boundingBox.plus(new Point2D.Double(res.boundingBox.getMaxX(), res.boundingBox.getMinY() - settings.font.getSize2D() / 8.0))
			    FormulaRenderingResult(boundingBox2, visualTop - settings.font.getSize2D() / 8.0, glyphs, glyphCoordinates, inversionLines2)
			  }
			}
	  }) (x)

	  
	  null
	}
}
