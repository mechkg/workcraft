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
  object VisualSignalTransition {
    val font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
	
	def graphicalContent(text : String, t : SignalType, background : Option[Color]) = {
	  val color = t match {
        case SignalType.Internal => Color.GREEN.darker
        case SignalType.Output => Color.BLUE.darker
        case SignalType.Input => Color.RED.darker
	  }
	  
	  def zeroCentered(img : RichGraphicalContent) = img.align(rectangle(0,0,None,None), HorizontalAlignment.Center,VerticalAlignment.Center)
	  
	  val label = zeroCentered(Graphics.label(text, font, color))
	  label.over(rectangle(label.visualBounds.getWidth, label.visualBounds.getHeight, None, background))
	}
  }
  
  def place(p : Place) = {
    circle(1, Some((new BasicStroke(0.1.toFloat), Color.BLACK)), Some(Color.WHITE)) // todo: tokens
  }
  
  def transition(t : Id[Transition])(stg : VisualStg) = {
    for(
        t <- stg.math.transitions.lookup(t);
        res <- t match {
          case DummyTransition => throw new NotImplementedException();
          case SignalTransition(signalId, direction) => 
            for(sig <- stg.math.signals.lookup(signalId)) yield
            VisualSignalTransition.graphicalContent(sig.name + direction, sig.direction, None)// TODO: Background for simulation
        }
    )
    yield res
  }
}
