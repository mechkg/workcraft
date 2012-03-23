package org.workcraft.gui.modeleditor
import org.workcraft.services.Service
import org.workcraft.services.ModelScope
import scalaz.NonEmptyList
import tools._
import org.workcraft.services.EditorServiceProvider

object EditorService extends Service[ModelScope, ModelEditor]

trait ModelEditor extends EditorServiceProvider {
  def tools: scalaz.NonEmptyList[ModelEditorTool] // this is not a service because providing this is mandatory
}