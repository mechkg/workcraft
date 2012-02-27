package org.workcraft.gui.modeleditor
import java.awt.event.MouseWheelListener
import java.awt.event.MouseWheelEvent
import org.workcraft.scala.effects.IO

trait IOMouseWheelListener extends MouseWheelListener {
  def mouseWheelMovedAction (e: MouseWheelEvent) : IO[Unit]
  
  final def mouseWheelMoved(e:MouseWheelEvent) = mouseWheelMovedAction(e).unsafePerformIO 
}