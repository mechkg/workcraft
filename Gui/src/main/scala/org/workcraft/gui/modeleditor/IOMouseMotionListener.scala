package org.workcraft.gui.modeleditor
import java.awt.event.MouseMotionListener
import java.awt.event.MouseEvent
import org.workcraft.scala.effects.IO

trait IOMouseMotionListener extends MouseMotionListener {
  def mouseDraggedAction (e: MouseEvent): IO[Unit]
  def mouseMovedAction (e: MouseEvent): IO[Unit]
  
  final def mouseDragged(e: MouseEvent) = mouseDraggedAction(e).unsafePerformIO
  final def mouseMoved(e: MouseEvent) = mouseMovedAction(e).unsafePerformIO
}