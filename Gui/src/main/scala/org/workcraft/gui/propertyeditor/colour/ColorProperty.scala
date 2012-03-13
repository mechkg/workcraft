package org.workcraft.gui.propertyeditor.colour

import java.awt.Color

import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.scala.Expressions._

object ColorProperty {
  def create(name: String, property: ModifiableExpression[Color]): Expression[EditableProperty] = property.map(value => {
    EditableProperty(name, ColorCellEditor, ColorCellRenderer, value, property.set(_: Color))
  })
}
