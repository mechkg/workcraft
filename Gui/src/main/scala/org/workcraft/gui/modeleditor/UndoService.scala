package org.workcraft.gui.modeleditor
import org.workcraft.services.Service
import org.workcraft.services.EditorScope
import org.workcraft.scala.Expressions.Expression
import org.workcraft.scala.effects.IO

object UndoService extends Service[EditorScope, Undo] 

case class Undo (undo: Expression[Option[UndoAction]], redo: Expression[Option[UndoAction]]) 

case class UndoAction (description: String, action: IO[Unit])
