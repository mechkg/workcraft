package org.workcraft.gui.propertyeditor.integer

import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.dependencymanager.advanced.core.ModifiableExpressionCombinator
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.string.StringProperty


object IntegerProperty {
  def create(name:String, property:ModifiableExpression[Integer]):EditableProperty = {
    return StringProperty.create(name, convertToString(property))
  }

  private def convertToString(property:ModifiableExpression[Integer]):ModifiableExpression[String] = {
    return Expressions.bind(property, new ModifiableExpressionCombinator[IntegerString])
  }

}
