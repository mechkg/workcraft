package org.workcraft.plugins.stg21
import java.awt.Color
import java.awt.Font
import org.workcraft.plugins.stg21.types.SignalType
import org.workcraft.plugins.stg21.types._
import org.workcraft.exceptions.NotImplementedException
import java.awt.BasicStroke
import java.awt.geom.Point2D
import org.workcraft.plugins.petri2.TokenPainter
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz._
import org.workcraft.graphics.Graphics
import org.workcraft.graphics.stg.RichGraphicalContent
import org.workcraft.graphics.stg.NotSoRichGraphicalContent._
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.Java2DDecoration._
import java.awt.Stroke
import RichGraphicalContent._
import org.workcraft.gui.CommonVisualSettings

object Visual {
  val font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
  
  object VisualSignalTransition {
	
	def graphicalContent(text : String, t : SignalType, background : Option[Color]) = {
	  val color = t match {
        case SignalType.Internal => Color.GREEN.darker
        case SignalType.Output => Color.BLUE.darker
        case SignalType.Input => Color.RED.darker
	  }
	  val lbl = label(text, font, color).zeroCentered
	  lbl.over(Graphics.rectangle(lbl.bcgc.bounds.rect.getWidth, lbl.bcgc.bounds.rect.getHeight, None, background).boundedColorisableGraphicalContent)
	}
  }
  object VisualDummyTransition {
    def graphicalContent(text : String, background : Option[Color]) = {
	  val lbl = label(text, font, Color.BLACK).zeroCentered
	  lbl.over(Graphics.rectangle(lbl.bcgc.bounds.rect.getWidth, lbl.bcgc.bounds.rect.getHeight, None, background).boundedColorisableGraphicalContent)
    }
  }
  
  def place(p : ExplicitPlace) : RichGraphicalContent = {
    var circ = circle(1, Some((new BasicStroke(0.1.toFloat), Color.BLACK)), Some(Color.WHITE))
    s => circ.under(TokenPainter.image(p.initialMarking, s))
  }
  
  def transition(t : Id[Transition])(stg : VisualStg) : Option[RichGraphicalContent] = {
    for(
        (t,inst) <- stg.math.transitions.lookup(t);
        res <- t match {
          case DummyLabel(name) => Some(VisualDummyTransition.graphicalContent(name, None))
          case SignalLabel(signalId, direction) => 
            for(sig <- stg.math.signals.lookup(signalId)) yield
            VisualSignalTransition.graphicalContent(sig.name + direction.symbol, sig.direction, None)// TODO: Background for simulation
        }
    )
    yield _ => res
  }
}
