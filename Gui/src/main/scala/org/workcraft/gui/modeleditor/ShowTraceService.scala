package org.workcraft.gui.modeleditor
import org.workcraft.services.Service
import org.workcraft.services.EditorScope
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.gui.modeleditor.tools.ModelEditorToolInstance
import org.workcraft.scala.effects.IO
import org.workcraft.gui.modeleditor.tools.ToolEnvironment

object ShowTraceService extends Service[EditorScope, ShowTrace]

trait ShowTrace {
  def show (trace: List[String]): (ModelEditorTool, ToolEnvironment => IO[ModelEditorToolInstance])
}