/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see [http://www.gnu.org/licenses/].
*
*/

package org.workcraft.gui.graph.tools.selection

import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Point2D
import javax.swing.Icon
import org.workcraft.gui.events.GraphEditorMouseEvent
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.DragHandle
import org.workcraft.gui.graph.tools.DragHandler
import org.workcraft.gui.graph.tools.DummyMouseListener
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.util.Geometry
import org.workcraft.scala.grapheditor.tools.HitTester
import org.workcraft.scala.Expressions._
import java.awt.event.InputEvent
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool.SelectionMode
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.Button
import org.workcraft.gui.GUI
import org.workcraft.gui.modeleditor.ToolMouseListener
import org.workcraft.scala.effects.IO
import scalaz.Scalaz._
import org.workcraft.gui.modeleditor.MouseButton
import org.workcraft.gui.modeleditor.Modifier

object GenericSelectionTool {
  sealed trait SelectionMode
  object SelectionMode {
    object None extends SelectionMode
    object Add extends SelectionMode
    object Remove extends SelectionMode
    object Replace extends SelectionMode
  }

  val noDrag = new DragHandle {
    override def setOffset(offset : Point2D.Double) = {}
    override def commit = {}
    override def cancel = {}
  }
    
  val button = new Button {
    override def label = "Selection tool"
    override def hotkey = Some(KeyEvent.VK_S)
    override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/select.svg").unsafePerformIO) 
  }
}

class GenericSelectionTool[Node](
      selection : ModifiableExpression[Set[Node]],
      hitTester : HitTester[Node],
      nodeDragHandler : DragHandler[Node]) {
  
  import GenericSelectionTool._
  
  var notClick1 : Boolean = false
  var notClick3 : Boolean = false
  
  private var currentDrag : DragHandle = noDrag
  val selectDragHandler = new SelectionDragHandler[Node](selection, hitTester)
  
  def isDragging = currentDrag!=noDrag
  
  def mouseClicked(e : GraphEditorMouseEvent) : Unit = {

    if(notClick1 && e.getButton == MouseEvent.BUTTON1)
      return
    if(notClick3 && e.getButton == MouseEvent.BUTTON3)
      return
    
    if(e.getButton==MouseEvent.BUTTON1) {
      hitTester.hitTest(e.getPosition) match {

        case Some(node) => {
          e.getKeyModifiers match {
            case 0 => selection.setValue(Set(node))
            case InputEvent.SHIFT_DOWN_MASK => selection.setValue(unsafeEval(selection) + node)
            case InputEvent.CTRL_DOWN_MASK => selection.setValue(unsafeEval(selection) - node)
          }
        }
        case None => if (e.getKeyModifiers==0)
            selection.setValue(Set.empty)
      }
    }
  }
  
  def mouseMoved(e : GraphEditorMouseEvent) =
    currentDrag.setOffset(Geometry.subtract(e.getPosition, e.getStartPosition))

  def startDrag(e : GraphEditorMouseEvent) = {
    assert(!isDragging)
    if(e.getButtonModifiers==InputEvent.BUTTON1_DOWN_MASK) {
      hitTester.hitTest(e.getStartPosition) match {

        case Some(hitNode) => {
          // hit something
          if(e.getKeyModifiers==0) {
            // mouse down without modifiers, begin move-drag
            if(hitNode!=null && !unsafeEval(selection).contains(hitNode))
              selection.setValue(Set(hitNode))

            currentDrag = nodeDragHandler.startDrag(hitNode)
          }
          // do nothing if pressed on a node with modifiers
        }

        case None => {
          // hit nothing, so start select-drag
          
          val mode = e.getKeyModifiers match {
            case 0 => SelectionMode.Replace
            case InputEvent.CTRL_DOWN_MASK => SelectionMode.Remove
            case InputEvent.SHIFT_DOWN_MASK => SelectionMode.Add
            case _ => SelectionMode.None
          }
          
          if(mode!=SelectionMode.None) {
            // selection will not actually be changed until drag completes
            currentDrag = selectDragHandler.startDrag(e.getStartPosition, mode)
          }
        }
      }
    }
  }

  def mousePressed(e : GraphEditorMouseEvent) = {
    if(e.getButton==MouseEvent.BUTTON1)
      notClick1 = false
    
    if(e.getButton==MouseEvent.BUTTON3) {
      
      if(isDragging) {
        cancelDrag(e)
        notClick1 = true
        notClick3 = true
      }
      else {
        notClick3 = false
      }
    }
  }
  
  def finishDrag(e : GraphEditorMouseEvent) = {
    currentDrag.commit
    currentDrag = noDrag
  }
  
  def cancelDrag(e : GraphEditorMouseEvent) = {
    currentDrag.cancel
    currentDrag = noDrag
  }

  def userSpaceContent(viewPort : Viewport) : Expression[GraphicalContent] =
    selectDragHandler.graphicalContent(viewPort)

  def effectiveSelection : Expression[Set[Node]] =
    selectDragHandler.effectiveSelection
  
  def getMouseListener : ToolMouseListener = {
    val me = this
    new ToolMouseListener {
      override def mousePressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double) : IO[Unit] = {}.pure[IO]
      override def mouseReleased(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double) : IO[Unit] = {}.pure[IO]
      override def mouseClicked(button: MouseButton, clickCount: Int, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = 
        IO.ioPure.pure(me.mouseClicked(e))
      override def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = 
        IO.ioPure.pure(me.mouseMoved(e))
      override def mouseEntered(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
      override def mouseExited(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]

      override def finishDrag(e : GraphEditorMouseEvent) = me.finishDrag(e)
      override def isDragging : Boolean = me.isDragging
      override def mousePressed(e : GraphEditorMouseEvent) = me.mousePressed(e)
      override def mouseMoved(e : GraphEditorMouseEvent) = me.mouseMoved(e)
      override def startDrag(e : GraphEditorMouseEvent) = me.startDrag(e)
    }
  }
}
