package org.workcraft.gui.propertyeditor

import pcollections.PVector


trait Properties {
  def getProperties():PVector[EditableProperty]
}
