package org.workcraft.gui.propertyeditor

import org.workcraft.util.Action


trait GenericEditorProvider[T ] {
  def createEditor(initialValue:T, accept:Action, cancel:Action):GenericCellEditor[T]
}
