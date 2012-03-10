package org.workcraft.gui.propertyeditor

import pcollections.PVector


trait PropertyEditor {
  def setObject(o:List[EditableProperty]):Unit
  def clearObject():Unit
}
