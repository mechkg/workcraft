package org.workcraft.gui.modeleditor.tools
import java.awt.geom.Point2D
import org.workcraft.scala.effects.IO

trait DragHandler[A] {
  def dragStarted(pos: Point2D.Double, node: A): DragHandle
}

trait DragHandle {
  def dragged(pos: Point2D.Double): IO[Unit]
  def commit: IO[Unit]
  def cancel: IO[Unit]
}