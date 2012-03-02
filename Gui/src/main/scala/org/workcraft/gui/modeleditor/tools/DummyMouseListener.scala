package org.workcraft.gui.modeleditor.tools

import org.workcraft.scala.effects.IO
import scalaz.Scalaz._

import org.workcraft.gui.modeleditor.ToolMouseListener
import java.awt.geom.Point2D
import scala.collection.immutable.Set
import org.workcraft.gui.modeleditor.MouseButton
import org.workcraft.gui.modeleditor.Modifier

class DummyMouseListener extends ToolMouseListener {
  def mousePressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def mouseReleased(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def mouseClicked(button: MouseButton, clickCount: Int, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def mouseEntered(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def mouseExited(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def startDrag(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) : IO[Unit] = {}.pure[IO]
  def finishDrag(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) : IO[Unit] = {}.pure[IO]
}
