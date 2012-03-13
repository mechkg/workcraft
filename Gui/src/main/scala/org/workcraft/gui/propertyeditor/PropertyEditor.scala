package org.workcraft.gui.propertyeditor

trait PropertyEditor {
  def setObject(o:List[EditableProperty]):Unit
  def clearObject():Unit
}
