package org.workcraft.gui.propertyeditor.string

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.table.DefaultTableCellRenderer
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.gui.propertyeditor.GenericEditorProvider
import org.workcraft.gui.propertyeditor.RendererProvider
import org.workcraft.util.Action


object StringProperty {
  def create(name:String, property:ModifiableExpression[String]):EditableProperty = {
    return EditableProperty.Util.create(name, EDITOR_PROVIDER, RENDERER_PROVIDER, property)
  }
  val EDITOR_PROVIDER:GenericEditorProvider[String] = new GenericEditorProvider[String]
  val RENDERER_PROVIDER:RendererProvider[String] = new RendererProvider[String]
}
