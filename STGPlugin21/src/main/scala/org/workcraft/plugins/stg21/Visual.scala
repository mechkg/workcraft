package org.workcraft.plugins.stg21
import java.awt.Color
import java.awt.Font
import org.workcraft.plugins.stg21.types.SignalType
import org.workcraft.plugins.stg21.types._
import org.workcraft.exceptions.NotImplementedException
import java.awt.BasicStroke
import java.awt.geom.Point2D
import org.workcraft.dom.visual.Touchable
import org.workcraft.plugins.petri21.TokenPainter
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz._
import org.workcraft.graphics.Graphics
import org.workcraft.graphics.RichGraphicalContent

object Visual {
  val font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
  
  object VisualSignalTransition {
	
	def graphicalContent(text : String, t : SignalType, background : Option[Color]) = {
	  val color = t match {
        case SignalType.Internal => Color.GREEN.darker
        case SignalType.Output => Color.BLUE.darker
        case SignalType.Input => Color.RED.darker
	  }
	  
	  val label = Graphics.label(text, font, color).zeroCentered
	  label.over(rectangle(label.visualBounds.getWidth, label.visualBounds.getHeight, None, background))
	}
  }
  object VisualDummyTransition {
    def graphicalContent(text : String, background : Option[Color]) = {
	  val label = Graphics.label(text, font, Color.BLACK).zeroCentered
	  label.over(rectangle(label.visualBounds.getWidth, label.visualBounds.getHeight, None, background))
    }
  }
  
  def place(p : ExplicitPlace) = {
    var circ = Graphics.circle(1, Some((new BasicStroke(0.1.toFloat), Color.BLACK)), Some(Color.WHITE))
    for(img <- TokenPainter.image(constant(p.initialMarking))) yield (img.over(circ, circ.touchable))
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
    yield res
  }
}
