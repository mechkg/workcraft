package org.workcraft.gui.propertyeditor.bool

import java.awt.Component
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JCheckBox
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.gui.propertyeditor.GenericEditorProvider
import org.workcraft.util.Action


object BooleanCellEditor {
  var INSTANCE:GenericEditorProvider[Boolean] = new GenericEditorProvider[Boolean]
}
