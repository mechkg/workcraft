package org.workcraft.gui.propertyeditor.string

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.table.DefaultTableCellRenderer

import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.gui.propertyeditor.GenericEditorProvider
import org.workcraft.gui.propertyeditor.RendererProvider
import org.workcraft.scala.effects.IO
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.scala.Expressions._

object StringProperty {
  def apply(name: String, property: ModifiableExpressionWithValidation[String, String]): Expression[EditableProperty] =  
    EditableProperty(name, EditorProvider, RendererProvider, property)
  
  def apply(name: String, property: ModifiableExpression[String]): Expression[EditableProperty] = property.map(value => {
    EditableProperty(name, EditorProvider, RendererProvider, value, property.set(_: String))
  })

  val EditorProvider = new GenericEditorProvider[String] {
    override def createEditor(initialValue: String, accept: IO[Unit], cancel: IO[Unit]): GenericCellEditor[String] = {
      val textField = new JTextField
      textField.setFocusable(true)
      textField.setText(initialValue)
      new GenericCellEditor[String] {
        override def component: Component = textField
        override def getValue: String = textField.getText()
      }
    }
  }

  val RendererProvider = new RendererProvider[String] {
    override def createRenderer(value: String): Component = {
      // TODO: think about missing features from DefaultTableCellRenderer
      val dtcr = new DefaultTableCellRenderer
      new JLabel(value)
    }
  };
}

object CustomStringProperty {

}