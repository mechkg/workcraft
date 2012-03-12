package org.workcraft.gui.propertyeditor

import java.awt.Component

trait GenericCellEditor[T] {
  def component():Component
  def getValue():T
}
