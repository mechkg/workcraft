package org.workcraft.gui.modeleditor.sim

import org.workcraft.scala.Expressions._
import javax.swing.Icon
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.exceptions.InvalidConnectionException
import scalaz._
import Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.GraphicalContent
import java.awt.event.InputEvent
import org.workcraft.gui.modeleditor.ToolMouseListener
import org.workcraft.gui.modeleditor.Viewport
import org.workcraft.gui.GUI
import org.workcraft.gui.modeleditor.Modifier
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.gui.modeleditor.MouseButton
import org.workcraft.gui.modeleditor.LeftButton
import org.workcraft.gui.modeleditor.RightButton
import org.workcraft.gui.modeleditor.tools.{ DummyMouseListener => DML }
import org.workcraft.graphics.Graphics
import java.awt.geom.Point2D
import org.workcraft.gui.modeleditor.tools.DummyMouseListener
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

class GenericSimulationToolMouseListener[Event](
  hitTester: Point2D.Double => IO[Option[Event]],
  enabled: IO[Event => Boolean],
  fire: Event => IO[Unit]) extends DummyMouseListener {
  private val mouseOverObject: ModifiableExpression[Option[Event]] = Variable.create[Option[Event]](None)

  override def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] =
    hitTester(position) >>= (n => mouseOverObject.set(n))

  override def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = button match {
    case LeftButton => mouseOverObject.eval >>= {
      case None => IO.Empty
      case Some(event) => enabled >>= (e => if (e(event)) fire(event) else IO.Empty)
    }
    case _ => IO.Empty
  }
}