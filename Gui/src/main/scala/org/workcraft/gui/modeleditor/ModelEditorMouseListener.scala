package org.workcraft.gui.modeleditor
import java.awt.event.MouseMotionListener
import java.awt.event.MouseEvent
import java.awt.Point
import java.awt.geom.Point2D
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseListener
import java.awt.event.MouseWheelListener

sealed trait MouseButton
case object LeftButton extends MouseButton
case object RightButton extends MouseButton
case class OtherButton(i: Int) extends MouseButton

trait ToolMouseListener {
  def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit]
  def buttonReleased(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit]
  def buttonClicked(button: MouseButton, clickCount: Int, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit]
  def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit]

  def dragStarted(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]): IO[Unit]
  def dragged(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]): IO[Unit]
  def dragFinished(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]): IO[Unit]
}

class ModelEditorMouseListener(val viewport: Viewport, val hasFocus: Expression[Boolean], val toolMouseListener: Expression[Option[ToolMouseListener]],
  val requestFocus: () => IO[Unit])
  extends MouseMotionListener with MouseListener with MouseWheelListener {

  val panButton = MouseEvent.BUTTON2

  def dragStartThreshold(button: Int) = button match {
    case btn if btn == panButton => 0 // start drag immediately for viewport pan
    case _ => 4 // use a threshold to generate drag events for the tools
  }

  def thresholdReached(button: Int, start: Point, position: Point) = start.distance(position) >= dragStartThreshold(button)

  private var prevPositionSS = new Point
  private var prevPositionUS = new Point2D.Double

  private val pressedButtons = new scala.collection.mutable.HashMap[Int, Point]()
  private val draggingButtons = new scala.collection.mutable.HashSet[Int]()

  def mouseWheelMoved(e: MouseWheelEvent) = {
    val p = new Point2D.Double(e.getPoint.getX, e.getPoint.getY)
    viewport.zoomTo(-e.getWheelRotation(), p).unsafePerformIO
  }

  def toUserSpace(p: Point) = {
    val screenToUser = viewport.screenToUser.unsafeEval
    screenToUser(new Point2D.Double(p.getX, p.getY))
  }

  def modifiers(e: MouseEvent): Set[Modifier] = Set((Modifier.Alt, e.isAltDown()), (Modifier.Shift, e.isShiftDown()), (Modifier.Control, e.isControlDown())).filter(_._2).map(_._1)

  def button(code: Int) = code match {
    case MouseEvent.BUTTON1 => LeftButton
    case MouseEvent.BUTTON3 => RightButton
    case i: Int => OtherButton(i)
  }

  def mouseMoved(e: MouseEvent) = {
    val currentPositionSS = e.getPoint
    val toolListener = toolMouseListener.unsafeEval

    if (pressedButtons.isEmpty)
      toolListener.map(_.mouseMoved(modifiers(e), toUserSpace(currentPositionSS))).foreach(_.unsafePerformIO)
    else pressedButtons.foreach {
      case (btn, pressedPosition) => {
        if (!draggingButtons.contains(btn) && thresholdReached(btn, pressedPosition, currentPositionSS)) {
          draggingButtons += btn

          if (btn != panButton) // middle button reserved for panning
            toolListener.map(_.dragStarted(button(btn), toUserSpace(pressedPosition), modifiers(e))).foreach(_.unsafePerformIO)
        }

        if (draggingButtons.contains(btn)) {
          if (btn == panButton)
            viewport.pan(currentPositionSS.x - prevPositionSS.x, currentPositionSS.y - prevPositionSS.y).unsafePerformIO
          else
            toolListener.map(_.dragged(button(btn), toUserSpace(currentPositionSS), modifiers(e))).foreach(_.unsafePerformIO)
        } else
          toolListener.map(_.mouseMoved(modifiers(e), toUserSpace(currentPositionSS))).foreach(_.unsafePerformIO)
      }
    }

    prevPositionSS.setLocation(currentPositionSS)
    prevPositionUS.setLocation(toUserSpace(currentPositionSS))
  }

  def mouseDragged(e: MouseEvent) = mouseMoved(e)

  def mouseClicked(e: MouseEvent) = {
    val toolListener = toolMouseListener.unsafeEval
    toolListener.map(_.buttonClicked(button(e.getButton), e.getClickCount(), modifiers(e), toUserSpace(e.getPoint()))).foreach(_.unsafePerformIO)
  }

  def mouseEntered(e: MouseEvent) = {}

  def mouseExited(e: MouseEvent) = {}

  def mousePressed(e: MouseEvent) = {
    if (!hasFocus.unsafeEval) requestFocus().unsafePerformIO

    pressedButtons += ((e.getButton(), e.getPoint()))

    if (e.getButton() != panButton) {
      val toolListener = toolMouseListener.unsafeEval
      toolListener.map(_.buttonPressed(button(e.getButton()), modifiers(e), toUserSpace(e.getPoint()))).foreach(_.unsafePerformIO)
    }
  }

  def mouseReleased(e: MouseEvent) = {
    pressedButtons -= e.getButton

    if (e.getButton() != panButton) {
      val toolListener = toolMouseListener.unsafeEval
      toolListener.map(_.buttonReleased(button(e.getButton()), modifiers(e), toUserSpace(e.getPoint()))).foreach(_.unsafePerformIO)
    }

    if (draggingButtons.contains(e.getButton())) {
      if (e.getButton() != panButton) {
        val toolListener = toolMouseListener.unsafeEval
        toolListener.map(_.dragFinished(button(e.getButton), toUserSpace(e.getPoint), modifiers(e))).foreach(_.unsafePerformIO)
      }
      draggingButtons -= e.getButton
    }
  }
}