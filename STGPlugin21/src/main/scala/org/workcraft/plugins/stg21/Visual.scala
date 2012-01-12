package org.workcraft.plugins.stg21
import java.awt.Color
import java.awt.Font
import org.workcraft.graphics.Graphics
import org.workcraft.plugins.stg21.types.SignalType
import org.workcraft.graphics.RichGraphicalContent
import org.workcraft.plugins.stg21.types._
import org.workcraft.exceptions.NotImplementedException
import java.awt.BasicStroke
import org.workcraft.graphics.Graphics._

object Visual {
  val font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
  def zeroCentered(img : RichGraphicalContent) = img.align(rectangle(0,0,None,None), HorizontalAlignment.Center,VerticalAlignment.Center)

  object VisualSignalTransition {
	
	def graphicalContent(text : String, t : SignalType, background : Option[Color]) = {
	  val color = t match {
        case SignalType.Internal => Color.GREEN.darker
        case SignalType.Output => Color.BLUE.darker
        case SignalType.Input => Color.RED.darker
	  }
	  
	  
	  val label = zeroCentered(Graphics.label(text, font, color))
	  label.over(rectangle(label.visualBounds.getWidth, label.visualBounds.getHeight, None, background))
	}
  }
  object VisualDummyTransition {
    def graphicalContent(text : String, background : Option[Color]) = {
	  val label = zeroCentered(Graphics.label(text, font, Color.BLACK))
	  label.over(rectangle(label.visualBounds.getWidth, label.visualBounds.getHeight, None, background))
    }
  }
  
  def place(p : ExplicitPlace) = {
    circle(1, Some((new BasicStroke(0.1.toFloat), Color.BLACK)), Some(Color.WHITE)) // todo: tokens
  }
  
  def transition(t : Id[Transition])(stg : VisualStg) : Option[RichGraphicalContent] = {
    for(
        (t,inst) <- stg.math.transitions.lookup(t);
        res <- t match {
          case DummyLabel(name) => Some(VisualDummyTransition.graphicalContent(name, None))
          case SignalLabel(signalId, direction) => 
            for(sig <- stg.math.signals.lookup(signalId)) yield
            VisualSignalTransition.graphicalContent(sig.name + direction, sig.direction, None)// TODO: Background for simulation
        }
    )
    yield res
  }
}
