package org.workcraft.services
import org.workcraft.scala.effects.IO

object UndoService extends Service[ModelScope, UndoImpl]

trait UndoImpl {
  def undo: Option[IO[Unit]]
}