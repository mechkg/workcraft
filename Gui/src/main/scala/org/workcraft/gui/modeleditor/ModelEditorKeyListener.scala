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
import java.awt.Window
import javax.swing.JOptionPane
import javax.swing.JPanel

sealed trait Modifier

object Modifier {
  case object Control extends Modifier
  case object Shift extends Modifier
  case object Alt extends Modifier
}

sealed trait KeyEventType

object KeyEventType {
  case object KeyPressed extends KeyEventType
  case object KeyReleased extends KeyEventType
  case object KeyTyped extends KeyEventType
}

case class KeyEvent(keyCode: Int, keyChar: Char, eventType: KeyEventType, modifiers: Set[Modifier])

object KeyEvent {
  import Modifier._
  import KeyEventType._
  
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

case class KeyBinding(description: String, keyCode: Int, eventType: KeyEventType, modifiers: Set[Modifier], action: IO[Option[String]])

case class HotkeyBinding(keyCode: Int, action: IO[Unit])

class ModelEditorKeyListener(editorWindow: JPanel, editorKeys: List[KeyBinding], toolKeys: Expression[List[KeyBinding]], hotkeys: List[HotkeyBinding], logger: () => Logger[IO]) extends KeyListener {
  import Modifier._
  import KeyEventType._

  def handlers(event: KeyEvent) = for {
    toolKeys <- toolKeys
  } yield (editorKeys ++ hotkeys.map(k => KeyBinding("Tool hotkey: " + k.keyCode.toChar, k.keyCode, KeyPressed, Set(), k.action >| None)) ++ toolKeys)
    .filter(h => (h.keyCode == event.keyCode && h.modifiers == event.modifiers && h.eventType == event.eventType))

  def handleEvent(event: KeyEvent) = {
    def execute (io: IO[Option[String]]) = io.unsafePerformIO match {
      case Some(s) => JOptionPane.showMessageDialog(editorWindow, s, "Error", JOptionPane.ERROR_MESSAGE)
      case _ => {}
    }
    
    val h = handlers(event).unsafeEval
    h match {
      case Nil => {}
      case head::Nil => execute(head.action)
      case _ => {
        logger().log("Key conflict! \"" + KeyEvent.show(event) + "\" is bound to: " + h.map(_.description).map("\"" + _ + "\"").reduceLeft(_ + " and " + _), MessageClass.Warning).unsafePerformIO
        h.foreach(k => { println("Executing " + k.description); execute(k.action) })
      }
    }
  }

  def keyTyped(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyTyped))
  def keyPressed(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyPressed))
  def keyReleased(e: JKeyEvent): Unit = handleEvent(KeyEvent(e, KeyReleased))
}

object ModelEditorKeyListener {
  import Modifier._
  import KeyEventType._
  
  def defaultBindings(editor: ModelEditorPanel) = List(
    KeyBinding("Viewport pan left", JKeyEvent.VK_LEFT, KeyPressed, Set(Control), editor.view.pan(20, 0) >| None),
    KeyBinding("Viewport pan right", JKeyEvent.VK_RIGHT, KeyPressed, Set(Control), editor.view.pan(-20, 0) >| None),
    KeyBinding("Viewport pan up", JKeyEvent.VK_UP, KeyPressed, Set(Control), editor.view.pan(0, 20) >| None),
    KeyBinding("Viewport pan down", JKeyEvent.VK_DOWN, KeyPressed, Set(Control), editor.view.pan(0, -20) >| None),
    KeyBinding("Viewport zoom in", JKeyEvent.VK_EQUALS, KeyPressed, Set(), editor.view.zoom(1) >| None),
    KeyBinding("Viewport zoom out", JKeyEvent.VK_MINUS, KeyPressed, Set(), editor.view.zoom(-1) >| None))
}