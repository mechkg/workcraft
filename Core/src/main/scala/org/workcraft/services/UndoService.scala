package org.workcraft.services
import org.workcraft.scala.effects.IO
import org.workcraft.scala.Expressions.Expression

object UndoService extends Service[ModelScope, Undo]

case class Undo (undo: Expression[Option[UndoAction]]) 

case class UndoAction (description: String, action: IO[Unit])
