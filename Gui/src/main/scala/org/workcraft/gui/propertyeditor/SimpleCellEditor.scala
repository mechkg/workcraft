package org.workcraft.gui.propertyeditor

import java.awt.Component


trait SimpleCellEditor {
  def commit():Unit
  def getComponent():Component
}
