package org.workcraft.gui.modeleditor.tools.selection

import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Point2D
import javax.swing.Icon

import org.workcraft.gui.modeleditor.Viewport

import org.workcraft.scala.grapheditor.tools.HitTester
import org.workcraft.scala.Expressions._
import java.awt.event.InputEvent
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.Button
import org.workcraft.gui.GUI
import org.workcraft.gui.modeleditor.ToolMouseListener
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._
import org.workcraft.gui.modeleditor.MouseButton
import org.workcraft.gui.modeleditor.Modifier
import org.workcraft.gui.modeleditor.tools.DummyMouseListener
import org.workcraft.gui.modeleditor.LeftButton
import org.workcraft.gui.modeleditor.RightButton
import org.workcraft.gui.modeleditor.tools.DragHandler
import org.workcraft.gui.modeleditor.tools.DragHandle

object GenericSelectionToolMouseListener {
  sealed trait SelectionMode

  object SelectionMode {
    object None extends SelectionMode
    object Add extends SelectionMode
    object Remove extends SelectionMode
    object Replace extends SelectionMode
  }

 

  def mouseListener[Node](selection: ModifiableExpression[Set[Node]], hitTester: HitTester[Node], nodeDragHandler: DragHandler[Node], doubleClickHandler: Option[Node => IO[Unit]]) =
    new GenericSelectionToolMouseListener(selection, hitTester, nodeDragHandler, doubleClickHandler)
}

class GenericSelectionToolMouseListener[Node](
  selection: ModifiableExpression[Set[Node]],
  hitTester: HitTester[Node],
  nodeDragHandler: DragHandler[Node],
  doubleClickHandler: Option[Node => IO[Unit]]) extends DummyMouseListener {

  import GenericSelectionToolMouseListener._

  var notClick1: Boolean = false
  var notClick3: Boolean = false

  private var dragHandle: Option[DragHandle] = None

  val selectDragHandler = new SelectionDragHandler[Node](selection, hitTester)
  def isDragging = dragHandle.isDefined

  override def buttonClicked(button: MouseButton, clickCount: Int, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {
    if ((notClick1 && button == LeftButton) || (notClick3 && button == RightButton))
      IO.Empty
    else if (button == LeftButton) {
      hitTester.hitTest(position) match {
        case Some(node) => if (clickCount == 1)
	  (modifiers.contains(Modifier.Shift), modifiers.contains(Modifier.Control)) match {
            case (false, false) => selection.set(Set(node))
            case (true, false) => selection.update(_ + node)
            case (false, true) => selection.update(_ - node)
            case (true, true) => IO.Empty // both Shift and Control are down, such action is undefined
          } else if (clickCount == 2) {
	    doubleClickHandler match {
	      case Some(handler) => handler(node)
	      case _ => IO.Empty
	    }
	  } else IO.Empty
          case None if (modifiers.isEmpty) => selection.set(Set[Node]())
          case _ => IO.Empty
	}
    } else IO.Empty
  }

  override def dragStarted(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]): IO[Unit] = {
    assert(!isDragging)
    if (button == LeftButton) {
      hitTester.hitTest(position) match {
        case Some(hitNode) => {
          // hit something
          if (modifiers.isEmpty) {
            // mouse down without modifiers, begin move-drag
            dragHandle = Some(nodeDragHandler.dragStarted(position, hitNode))

            selection.eval >>= (sel => if (!sel.contains(hitNode)) selection.set(Set(hitNode)) else IO.Empty)
          } else
            // do nothing if pressed on a node with modifiers
            IO.Empty
        }

        case None => {
          // hit nothing, so start select-drag
          val mode = (modifiers.contains(Modifier.Shift), modifiers.contains(Modifier.Control)) match {
            case (false, false) => SelectionMode.Replace
            case (true, false) => SelectionMode.Add
            case (false, true) => SelectionMode.Remove
            case _ => SelectionMode.None
          }

          ioPure.pure(
            if (mode != SelectionMode.None) {
              // selection will not actually be changed until drag completes
              dragHandle = Some(selectDragHandler.startDrag(position, mode))
            })
        }
      }
    } else
      IO.Empty
  }

  override def dragged(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]): IO[Unit] = dragHandle match {
    case Some(handle) => handle.dragged(position)
    case None => IO.Empty
  }

  override def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = button match {
    // I wonder what sort of podgon is this? :)
    case LeftButton => ioPure.pure { notClick1 = false }
    case RightButton => if (isDragging)
      cancelDrag >>=| ioPure.pure { notClick1 = true; notClick3 = true }
    else
      ioPure.pure { notClick3 = false }
    case _ => IO.Empty
  }

  def finishDrag: IO[Unit] = dragHandle match {
    case Some(handle) => handle.commit >>=| ioPure.pure { dragHandle = None }
    case None => throw new RuntimeException("Attempt to commit on an empty drag handle")
  }

  def cancelDrag: IO[Unit] = dragHandle match {
    case Some(handle) => handle.cancel >>=| ioPure.pure { dragHandle = None }
    case None => throw new RuntimeException("Attempt to cancel on an empty drag handle")
  }

  override def dragFinished(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]): IO[Unit] = dragHandle match {
    case Some(handle) => handle.dragged(position) >>=| finishDrag
    case None => IO.Empty
  }

  def userSpaceContent(viewPort: Viewport): Expression[GraphicalContent] =
    selectDragHandler.graphicalContent(viewPort)

  def effectiveSelection: Expression[Set[Node]] =
    selectDragHandler.effectiveSelection
}
