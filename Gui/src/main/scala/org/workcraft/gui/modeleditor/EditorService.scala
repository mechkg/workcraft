package org.workcraft.gui.modeleditor
import org.workcraft.services.Service
import org.workcraft.services.ModelScope
import scalaz.NonEmptyList
import org.workcraft.gui.modeleditor.tools.ModelEditorTool

import tools._

object EditorService extends Service[ModelScope, ModelEditor]

trait ModelEditor {
  def tools: NonEmptyList[ModelEditorToolMaker]
}
