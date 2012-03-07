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
        mouseOverObject >>= {
          case None => constant(GraphicalContent.Empty)
          case Some(mouseOver) => {
            val color = connectionManager.connect(first, mouseOver) match {
              case Left(err) => { warningMessage.setValue(Some(err.getMessage)); Color.RED }
              case Right(_) => { Color.GREEN }
            }
            drawConnectingLine(color)
          }
        }
      }
    }

  private def drawConnectingLine(color: Color): Expression[GraphicalContent] = {
    first >>= {
      case None => throw new RuntimeException("Should not happen!")
      case Some(first) =>
        for (
          center <- centerProvider(first); lastCoords <- lastMouseCoords
        ) yield (
          new GraphicalContent {
            override def draw(g: Graphics2D) = {
              g.setColor(color)
              g.draw(new Line2D.Double(center.getX, center.getY, lastCoords.getX, lastCoords.getY))
            }
          })
    }
  }

  val mouseListener: ToolMouseListener = new DML {
    override def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] =
      set(lastMouseCoords, position) >>=|
        hitTester(position) >>= (n => {
          set(mouseOverObject, n) >>=|
            (if (!leftFirst && mouseExitRequiredForSelfLoop)
              eval(first) >>= (f => if (f == n) set(mouseOverObject, None) else ioPure.pure { leftFirst = true })
            else
              IO.Empty)
        })

    override def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = button match {
      case LeftButton => eval(first) >>= {
        case None => eval(mouseOverObject) >>= {
          case None => IO.Empty
          case Some(mouseOver) => assign(first, mouseOverObject) >>=| ioPure.pure { leftFirst = false } >>=| mouseMoved(modifiers, position)
        }
        case Some(currentFirst) => {
          eval(mouseOverObject) >>= {
            case None => IO.Empty
            case Some(mouseOver) => {
              connectionManager.connect(currentFirst, mouseOver) match {
                case Right(connect) => {
                  connect >>=| (
                    if (modifiers.contains(Modifier.Control)) assign(first, mouseOverObject) >>=| set(mouseOverObject, None)
                    else set(first, None))
                }
                case Left(err) => ioPure.pure { Toolkit.getDefaultToolkit.beep }
              }
            }
          }
        }
      }
      case RightButton => set(first, None) >>=| set(mouseOverObject, None)
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
