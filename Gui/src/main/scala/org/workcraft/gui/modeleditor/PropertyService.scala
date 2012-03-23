package org.workcraft.gui.modeleditor
import org.workcraft.services.Service
import org.workcraft.services.EditorScope
import org.workcraft.scala.Expressions.Expression
import org.workcraft.gui.propertyeditor.EditableProperty

object PropertyService extends Service[EditorScope, Expression[List[Expression[EditableProperty]]]]