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

class ModelEditorKeyListener(
  editorKeys: Expression[List[KeyBinding]],
  toolKeys: Expression[List[KeyBinding]],
  hotkeys: Expression[List[HotkeyBinding]],
  logger: () => Logger[IO]) extends KeyListener {

  def handlers(event: KeyEvent) = for {
    editorKeys <- editorKeys
    hotkeys <- hotkeys;
    toolKeys <- toolKeys
  } yield {
    def f(l: List[KeyBinding]) = l.filter(h => (h.keyCode == event.keyCode && h.modifiers == event.modifiers && h.eventType == event.eventType))

    f(editorKeys) ++ f(hotkeys.map(k => KeyBinding("Tool hotkey: " + k.keyCode.toChar, k.keyCode, KeyPressed, Set(), k.action))) ++ f(toolKeys)
  }

  def handleEvent(event: KeyEvent) = {
    val h = unsafeEval(handlers(event))
    h.length match {
      case 0 => {}
      case 1 => h.head.action.unsafePerformIO
      case _ => {
        val l = logger()
        l.log("Key conflict! \"" + KeyEvent.show(event) + "\" is bound to: " + h.map(_.description).reduceLeft("\"" + _ + ", " + _ + "\""), MessageClass.Warning)
        h.foreach(_.action.unsafePerformIO)
      }
    }
  }

  def keyTyped(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyTyped))
  def keyPressed(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyPressed))
  def keyReleased(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyReleased))

  /*{
    val editorAction = if (e.isControlDown()) None else e.getKeyCode match {
      case KeyEvent.VK_LEFT => Some(editor.view.pan(20, 0))
      case KeyEvent.VK_RIGHT => Some(editor.view.pan(-20, 0))
      case KeyEvent.VK_UP => Some(editor.view.pan(0, 20))
      case KeyEvent.VK_DOWN => Some(editor.view.pan(0, -20))
      case KeyEvent.VK_EQUALS => Some(editor.view.zoom(1))
      case KeyEvent.VK_MINUS => Some(editor.view.zoom(-1))
      case _ => None
    }*/

  // This is the topmost key listener that handles common model editor key presses
  // if it knows how to handle a key press it will handle it and will not pass it to the
  // next handler to avoid key clashes.

  // The next handler is the tool box which listens for tool hotkeys. If it recognises a hotkey
  // it activates the corresponding tool and stops, if it does not recognise a hotkey it forwards
  // the event to the key handler provided by the model. 

  // FIXME: this should rather be done in a more explicit way so that the key strokes do not
  // silently disappear in the model key listener
}