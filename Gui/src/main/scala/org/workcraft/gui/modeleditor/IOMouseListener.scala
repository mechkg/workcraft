package org.workcraft.gui.modeleditor
import java.awt.event.MouseListener
import java.awt.event.MouseEvent

import org.workcraft.scala.effects.IO

trait IOMouseListener extends MouseListener {
  def mouseClickedAction (e: MouseEvent): IO[Unit]
  def mouseEnteredAction (e: MouseEvent): IO[Unit]
  def mouseExitedAction (e: MouseEvent): IO[Unit]
  def mousePressedAction (e: MouseEvent): IO[Unit]
  def mouseReleasedAction (e: MouseEvent): IO[Unit]
  
  final def mouseClicked (e: MouseEvent) = mouseClickedAction(e).unsafePerformIO
  final def mouseEntered (e: MouseEvent) = mouseEnteredAction(e).unsafePerformIO
  final def mouseExited (e: MouseEvent) = mouseExitedAction(e).unsafePerformIO
  final def mousePressed (e: MouseEvent) = mousePressedAction(e).unsafePerformIO
  final def mouseReleased (e: MouseEvent) = mouseReleasedAction(e).unsafePerformIO
}