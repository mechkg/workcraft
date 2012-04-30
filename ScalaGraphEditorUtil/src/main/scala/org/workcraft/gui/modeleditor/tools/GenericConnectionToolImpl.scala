package org.workcraft.gui.modeleditor.tools

import org.workcraft.scala.Expressions._
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import javax.swing.Icon
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.exceptions.InvalidConnectionException
import scalaz._
import Scalaz._
import org.workcraft.scala.Scalaz
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
import org.workcraft.gui.modeleditor.tools.{DummyMouseListener => DML}
import org.workcraft.graphics.Graphics

class GenericConnectionToolImpl[N](centerProvider: N => Expression[Point2D.Double],
  connectionManager: ConnectionManager[N],
  hitTester: Point2D.Double => IO[Option[N]]) {
  private val mouseOverObject: ModifiableExpression[Option[N]] = Variable.create[Option[N]](None)
  private val first: ModifiableExpression[Option[N]] = Variable.create[Option[N]](None)

  private var mouseExitRequiredForSelfLoop: Boolean = true
  private var leftFirst: Boolean = false
  private var lastMouseCoords: ModifiableExpression[Point2D.Double] = Variable.create(new Point2D.Double)
  private var warningMessage: ModifiableExpression[Option[String]] = Variable.create[Option[String]](None)

  val mouseOverNode: Expression[Option[N]] = mouseOverObject
  val firstNode: Expression[Option[N]] = first

  def connectingLineGraphicalContent(viewport: Viewport): Expression[GraphicalContent] =
    first >>= {
      case None => constant(GraphicalContent.Empty)
      case Some(first) => {
        warningMessage.setValue(None)
        mouseOverObject >>= (mouseOverObject =>
          {
            def zogo : Expression[(Color, Point2D.Double)] = (mouseOverObject match {
              case None => lastMouseCoords.map ((Color.BLUE, _))
              case Some(second) => connectionManager.connect(first, second) >>= {
                case Left(err) => { warningMessage.setValue(Some(err.getMessage)); lastMouseCoords.map((Color.RED,_)) }
                case Right(_) => centerProvider(second).map((Color.GREEN,_))
              }})

            for(
              ogoz <- zogo;
              p1 <- centerProvider(first);
              p2 = ogoz._2;
	      color = ogoz._1;
              px <- viewport.pixelSizeInUserSpace
            ) yield 
	      (Graphics.line(p1, p2, new BasicStroke(px.getX.toFloat), color).graphicalContent)
          })
        }
      }
    

  val mouseListener: ToolMouseListener = new DML {
    override def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] =
      lastMouseCoords.set(position) >>=|
        hitTester(position) >>= (n => {
          mouseOverObject.set(n) >>=|
            (if (!leftFirst && mouseExitRequiredForSelfLoop)
              first.eval >>= (f => if (f == n) mouseOverObject.set(None) else ioPure.pure { leftFirst = true })
            else
              IO.Empty)
        })

    override def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = button match {
      case LeftButton => first.eval >>= {
        case None => mouseOverObject.eval >>= {
          case None => IO.Empty
          case Some(mouseOver) => (first := mouseOverObject) >>=| ioPure.pure { leftFirst = false } >>=| mouseMoved(modifiers, position)
        }
        case Some(currentFirst) => {
          mouseOverObject.eval >>= {
            case None => IO.Empty
            case Some(mouseOver) => {
              connectionManager.connect(currentFirst, mouseOver).eval >>= {
                case Right(connect) => {
                  connect >>=| (
                    if (modifiers.contains(Modifier.Control)) (first := mouseOverObject) >>=| mouseOverObject.set(None)
                    else first.set(None))
                }
                case Left(err) => ioPure.pure { Toolkit.getDefaultToolkit.beep }
              }
            }
          }
        }
      }
      case RightButton => first.set(None) >>=| mouseOverObject.set(None)
      case _ => IO.Empty
    }
  }

  def screenSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]): Expression[GraphicalContent] =
    hasFocus >>= {
      case false => constant(GraphicalContent.Empty)
      case true => (warningMessage >>= {
        case Some(msg) => constant((Color.RED, msg))
        case None => first >>= {
          case None => constant((Color.BLACK, "Click on the first component"))
          case Some(_) => constant((Color.BLACK, "Click on the second component (control+click to connect continuously)"))
        }
      }) >>= (msg => GUI.editorMessage(viewport, msg._1, msg._2))
    }

  def deactivated = {
    first.setValue(None)
    mouseOverObject.setValue(None)
  }
}
