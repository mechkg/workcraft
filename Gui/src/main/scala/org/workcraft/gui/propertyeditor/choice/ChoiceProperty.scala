package org.workcraft.gui.propertyeditor.choice

import java.awt.Component
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.gui.propertyeditor.GenericEditorProvider
import org.workcraft.gui.propertyeditor.RendererProvider
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.util.Action
import org.workcraft.util.Pair
import pcollections.PVector


object ChoiceProperty {
  private final def createEditorProvider(choice:PVector[Pair[StringT]]):GenericEditorProvider[T] = {
    return new GenericEditorProvider[T]
  }

  private final def createRendererProvider(choice:PVector[Pair[StringT]]):RendererProvider[T] = {
    return new RendererProvider[T]
  }

  def create(name:String, choice:PVector[Pair[StringT]], property:ModifiableExpression[T]):EditableProperty = {
    return EditableProperty.Util.create(name, createEditorProvider(choice), createRendererProvider(choice), property)
  }

}
