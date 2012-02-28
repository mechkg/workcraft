import org.workcraft.exceptions.NotImplementedException
import java.awt.font.GlyphVector
import java.awt.font.TextAttribute
import org.workcraft.exceptions.NotSupportedException
import java.awt.font.FontRenderContext
import java.awt.{Font => JavaFont}
import java.awt.geom.Rectangle2D
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions._
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import scalaz.Monad
import org.workcraft.scala.Scalaz._ 
import scala.collection.JavaConversions._
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.Java2DDecoration._

package org.workcraft.graphics {
package object formularendering {

	class UseUnicode(v : Boolean) {
	  val value = v
	}
	
	implicit def useUnicode(value : Boolean) : UseUnicode = new UseUnicode(value)
	implicit val defaultUseUnicode : UseUnicode = true
	
	object FormulaToGraphics {
	  val withPodgonFontRenderContext = FormulaToGraphics(PodgonFontRenderContext) 
	  lazy val defaultFont = JavaFont.createFont(JavaFont.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb")).deriveFont(0.5f);
	  lazy val fancyFont = JavaFont.createFont(JavaFont.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
	}
	
	case class FormulaToGraphics(fontRenderContext : FontRenderContext) {
	  
	  case class Font(font : JavaFont, defaultFont : JavaFont) {
	    def deriveSubscript : Font = Font(Font.deriveSubscriptJava(font), Font.deriveSubscriptJava(defaultFont))
	    def createGlyphVector(c : Char) : GlyphVector = {
	      val fnt = if(font.canDisplay(c)) font else defaultFont
	      fnt.createGlyphVector(fontRenderContext, Array(c))
	    }
	    def size =  font.getSize2D
	  }
	  object Font {
		  def deriveSubscriptJava(font : JavaFont) = {
			val attributes = Map((TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB))
			font.deriveFont(attributes);
		  }
	  }
	  
	  val withFancyFont = WithFont(FormulaToGraphics.fancyFont)
	  
	  object WithFont {
	        def apply(font : JavaFont) = new WithFont(font)
	  }
	  
	  class WithFont(font : Font) {
	    
	    def this(font : JavaFont) = this(Font(font, FormulaToGraphics.defaultFont.deriveFont(font.getSize2D)))
	    
		def print(text : String) : FormulaRenderingResult =
			if (text.length() < 1) print(" ")
			else {
				val subfont = font.deriveSubscript
				
				def render(font : Font, text : String) = {
				  text.map(c => print(font, c)).foldLeft(FormulaRenderingResult.empty)(_ plus _)
				}
				
				text.lastIndexOf('_') match {
				  case -1 => render(font, text)
				  case x => render(font, text.substring(0, x)) plus render(subfont, text.substring(x+1))
				}
			}
		
	    def print(c : Char) : FormulaRenderingResult = print(font, c)
	    
		def print(font : Font, c : Char) : FormulaRenderingResult = {
			val glyphs = font.createGlyphVector(c)
			
			FormulaRenderingResult(glyphs.logicalBounds,
			    glyphs.visualBounds,
			    List((glyphs, new Point2D.Double(0, 0))),
			    List());
		}
	
		case class ImplicitFunc[A, B](func : A => B) {
		  def apply(implicit a : A) : B = func(a);
		}
		
		/*def render[Var](formula : BooleanFormula[Var]) = ImplicitFunc((useUnicode : UseUnicode) => ImplicitFunc((varPrinter : Var => String) => {
		  
		}))*/
	
		def renderM[M[_], Var] (formula : BooleanFormula[Var])(varPrinter : Var => M[String])(implicit monad : Monad[M]) : M[FormulaRenderingResult] = {
		  class DefaultPrinter(p : BooleanFormula[Var] => M[FormulaRenderingResult]) extends BooleanVisitor[Var, M[FormulaRenderingResult]] {
		    override def visit(v : Variable[Var]) = p(v)
		    override def visit(v : Iff[Var]) = p(v)
		    override def visit(v : Or[Var]) = p(v)
		    override def visit(v : And[Var]) = p(v)
		    override def visit(v : Xor[Var]) = p(v)
		    override def visit(v : Not[Var]) = p(v)
		    override def visit(v : Imply[Var]) = p(v)
		    override def visit(v : One[Var]) = p(v)
		    override def visit(v : Zero[Var]) = p(v)
		  }
		  def printIff(x : BooleanFormula[Var]) : M[FormulaRenderingResult] = (new DefaultPrinter(printImply(_)) {
		    override def visit(v : Iff[Var]) = printBinary(printIff(_), " = ", v)
		  }) (x);
		  def printImply(x : BooleanFormula[Var]) : M[FormulaRenderingResult] = (new DefaultPrinter(printOr(_)) {
		    override def visit(v : Imply[Var]) = printBinary(printOr(_), " => ", v)
		  }) (x)
		  def printOr(x : BooleanFormula[Var]) : M[FormulaRenderingResult] = (new DefaultPrinter(printXor(_)) {
		    override def visit(v : Or[Var]) = printBinary(printOr(_), " + ", v)
		  }) (x)
		  def printXor(x : BooleanFormula[Var]) : M[FormulaRenderingResult] = (new DefaultPrinter(printAnd(_)) {
		    override def visit(v : Xor[Var]) = printBinary(printXor(_), " ^ ", v)
		  }) (x)
		  def printAnd(x : BooleanFormula[Var]) : M[FormulaRenderingResult] = (new DefaultPrinter(printNot(_)) {
		    override def visit(v : And[Var]) = printBinary(printAnd(_), " ^ ", v)
		  }) (x)
		  def printBinary(printer : BooleanFormula[Var] => M[FormulaRenderingResult], opSymbol : String, formula : BinaryBooleanFormula[Var]) : M[FormulaRenderingResult] = {
			for (x <- printer(formula.getX()); y <- printer(formula.getY())) yield x plus print(opSymbol) plus y
		  }
		  
		  def printNot(x : BooleanFormula[Var]) : M[FormulaRenderingResult] = (new DefaultPrinter(printLiteral(_)) {
		    override def visit(node : Not[Var]) =
				for (x <- printIff(node.getX())) yield x match {
				  case FormulaRenderingResult(logicalBounds, visualBounds, glyphs, inversionLines) => {
				    val lineOffset = -font.size / 8.0
				    
				    val inversionLines2 = new Line2D.Double(
				    		visualBounds.getMinX(), visualBounds.getMinY + lineOffset,
				    		visualBounds.getMaxX(), visualBounds.getMinY + lineOffset) :: inversionLines
				    val logicalBounds2 = logicalBounds.plus(new Point2D.Double(logicalBounds.getMaxX(), logicalBounds.getMinY() + lineOffset))
				    FormulaRenderingResult(logicalBounds2, visualBounds.plus (new Point2D.Double(visualBounds.getMinX, visualBounds.getMinY + lineOffset)), glyphs, inversionLines2)
				  }
				}
		  }) (x)
		  
		  def printLiteral(x : BooleanFormula[Var]) : M[FormulaRenderingResult] = (new DefaultPrinter(printParentheses(_)) {
		    override def visit(zero : Zero[Var]) = monad.pure(print('0'))
		    override def visit(one : One[Var]) = monad.pure(print('1'))
		    override def visit(v : Variable[Var]) = for (str <- varPrinter(v.variable)) yield print(str)
		  }) (x)

		  def printParentheses(x : BooleanFormula[Var]) = for(x <- printIff(x)) yield print('(') plus x plus print(')')
		  
		  printIff(formula)
		}
	  }
	}
}
}