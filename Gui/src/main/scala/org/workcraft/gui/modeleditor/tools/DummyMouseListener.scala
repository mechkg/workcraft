package org.workcraft.gui.modeleditor.tools

import org.workcraft.scala.effects.IO
import scalaz.Scalaz._

import org.workcraft.gui.modeleditor.ToolMouseListener
import java.awt.geom.Point2D
import scala.collection.immutable.Set
import org.workcraft.gui.modeleditor.MouseButton
import org.workcraft.gui.modeleditor.Modifier

class DummyMouseListener extends ToolMouseListener {
  def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def buttonReleased(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def buttonClicked(button: MouseButton, clickCount: Int, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {}.pure[IO]
  
  def dragStarted(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) : IO[Unit] = {}.pure[IO]
  def dragged(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) : IO[Unit] = {}.pure[IO]
  def dragFinished(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) : IO[Unit] = {}.pure[IO]
}
