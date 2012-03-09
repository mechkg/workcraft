package org.workcraft.gui.propertyeditor.bool

import java.awt.Component
import javax.swing.JCheckBox
import org.workcraft.gui.propertyeditor.RendererProvider


object BooleanCellRenderer {
  var INSTANCE:RendererProvider[Boolean] = new RendererProvider[Boolean]
}
