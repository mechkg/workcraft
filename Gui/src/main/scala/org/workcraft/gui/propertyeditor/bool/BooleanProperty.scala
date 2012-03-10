package org.workcraft.gui.propertyeditor.bool

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.gui.propertyeditor.EditableProperty


object BooleanProperty {
  def create(name:String, expr:ModifiableExpression[Boolean]):EditableProperty = {
    return EditableProperty(name, BooleanCellEditor, BooleanCellRenderer, expr)
  }
}
