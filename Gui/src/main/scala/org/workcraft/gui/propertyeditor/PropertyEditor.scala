package org.workcraft.gui.propertyeditor

import pcollections.PVector


trait PropertyEditor {
  def setObject(o:PVector[EditableProperty]):Unit
  def clearObject():Unit
}
