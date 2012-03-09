package org.workcraft.gui.propertyeditor

import org.workcraft.util.Action


trait EditorProvider {
  def getEditor(close:Action):SimpleCellEditor
}
