package org.workcraft.gui.modeleditor
import org.workcraft.services.Service
import org.workcraft.services.ModelScope

object EditorService extends Service[ModelScope, ModelEditor]

trait ModelEditor