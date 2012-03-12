package org.workcraft.gui.propertyeditor.integer

import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.scala.Expressions._


object IntegerProperty {
  def apply(name:String, property:ModifiableExpression[Int]):EditableProperty = {
    return StringProperty(name, property.xmap(_.toString)(Integer.parseInt(_)))
  }
}
