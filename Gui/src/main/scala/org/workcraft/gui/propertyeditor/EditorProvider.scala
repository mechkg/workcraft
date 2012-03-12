package org.workcraft.gui.propertyeditor

import org.workcraft.util.Action
import org.workcraft.scala.effects.IO


trait EditorProvider {
  def getEditor(close:IO[Unit]):SimpleCellEditor
}
