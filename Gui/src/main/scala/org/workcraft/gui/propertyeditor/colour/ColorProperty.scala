package org.workcraft.gui.propertyeditor.colour

import java.awt.Color
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.gui.propertyeditor.EditableProperty


object ColorProperty {
  def create(name:String, property:ModifiableExpression[Color]):EditableProperty = {
    return EditableProperty.Util.create(name, new ColorCellEditor(), ColorCellRenderer.INSTANCE, property)
  }

}
