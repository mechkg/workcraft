package org.workcraft.gui.propertyeditor.bool

import java.awt.Component
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JCheckBox
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.gui.propertyeditor.GenericEditorProvider
import org.workcraft.util.Action

import org.workcraft.scala.effects.IO

object BooleanCellEditor extends GenericEditorProvider[Boolean] {
  override def createEditor(initialValue: Boolean, accept: IO[Unit], cancel: IO[Unit]): GenericCellEditor[Boolean] = {
    val checkBox = new JCheckBox()
    checkBox.setOpaque(false)
    checkBox.setFocusable(false)
    checkBox.setSelected(initialValue)
    checkBox.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent) {
        accept.unsafePerformIO
      }
    })
    new GenericCellEditor[Boolean] {
      override def component: Component = checkBox
      override def getValue: Boolean = checkBox.isSelected
    }
  }
}
