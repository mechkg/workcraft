package org.workcraft.gui.modeleditor

import java.awt.event.KeyListener
import java.awt.event.{ KeyEvent => JKeyEvent }
import org.workcraft.scala.effects.IO
import scalaz._
import Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.event.InputEvent
import org.workcraft.logging.Logger
import org.workcraft.logging.MessageClass

sealed trait Modifier

case object Control extends Modifier
case object Shift extends Modifier
case object Alt extends Modifier

sealed trait KeyEventType

case object KeyPressed extends KeyEventType
case object KeyReleased extends KeyEventType
case object KeyTyped extends KeyEventType

case class KeyEvent(keyCode: Int, keyChar: Char, eventType: KeyEventType, modifiers: Set[Modifier])

object KeyEvent {
  def show(ke: KeyEvent) = {
    (if (ke.modifiers.contains(Alt)) "Alt+" else "") +
      (if (ke.modifiers.contains(Control)) "Control+" else "") +
      (if (ke.modifiers.contains(Shift)) "Shift+" else "") +
      JKeyEvent.getKeyText(ke.keyCode) + (ke.eventType match {
        case KeyPressed => " pressed"
        case KeyReleased => " released"
        case KeyTyped => " typed"
      })
  }

  def apply(event: JKeyEvent, t: KeyEventType) = {
    val keyChar = event.getKeyChar
    val keyCode = event.getKeyCode

    val modifiers: Set[Modifier] = Set((Alt, event.isAltDown()), (Shift, event.isShiftDown()), (Control, event.isControlDown())).filter(_._2).map(_._1)

    new KeyEvent(keyCode, keyChar, t, modifiers)
  }
}

case class KeyBinding(description: String, keyCode: Int, eventType: KeyEventType, modifiers: Set[Modifier], action: IO[Unit])

case class HotkeyBinding(keyCode: Int, action: IO[Unit])

class ModelEditorKeyListener(editorKeys: List[KeyBinding], toolKeys: Expression[List[KeyBinding]], hotkeys: List[HotkeyBinding], logger: () => Logger[IO]) extends KeyListener {

  def handlers(event: KeyEvent) = for {
    toolKeys <- toolKeys
  } yield 
    (editorKeys ++ hotkeys.map(k => KeyBinding("Tool hotkey: " + k.keyCode.toChar, k.keyCode, KeyPressed, Set(), k.action)) ++ toolKeys)
    .filter(h => (h.keyCode == event.keyCode && h.modifiers == event.modifiers && h.eventType == event.eventType))
  

  def handleEvent(event: KeyEvent) = {
    val h = unsafeEval(handlers(event))
    h.length match {
      case 0 => {}
      case 1 => h.head.action.unsafePerformIO
      case _ => {
        logger().log("Key conflict! \"" + KeyEvent.show(event) + "\" is bound to: " + h.map(_.description).map("\""+_+"\"").reduceLeft(_+" and "+_) , MessageClass.Warning).unsafePerformIO
        h.foreach( k =>{println ("Executing " + k.description); k.action.unsafePerformIO})
      }
    }
  }

  def keyTyped(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyTyped))
  def keyPressed(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyPressed))
  def keyReleased(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyReleased))
}

object ModelEditorKeyListener {
  def defaultBindings (editor: ModelEditorPanel) = List (
      KeyBinding ("Viewport pan left", JKeyEvent.VK_LEFT, KeyPressed, Set(Control), editor.view.pan(20, 0)),
      KeyBinding ("Viewport pan right", JKeyEvent.VK_RIGHT, KeyPressed, Set(Control), editor.view.pan(-20, 0)),
      KeyBinding ("Viewport pan up", JKeyEvent.VK_UP, KeyPressed, Set(Control), editor.view.pan(0, 20)),
      KeyBinding ("Viewport pan down", JKeyEvent.VK_DOWN, KeyPressed, Set(Control), editor.view.pan(0, -20)),
      KeyBinding ("Viewport zoom in", JKeyEvent.VK_EQUALS, KeyPressed, Set(), editor.view.pan(0, -20)),
      KeyBinding ("Viewport zoom out", JKeyEvent.VK_MINUS, KeyPressed, Set(), editor.view.pan(0, -20))
      )
}