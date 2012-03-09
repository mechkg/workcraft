package org.workcraft.gui.propertyeditor.colour

import java.awt.Color
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JLabel
import org.workcraft.gui.propertyeditor.RendererProvider


object ColorCellRenderer {
  private val isBordered:Boolean = true
  var INSTANCE:RendererProvider[Color] = new RendererProvider[Color]
}
