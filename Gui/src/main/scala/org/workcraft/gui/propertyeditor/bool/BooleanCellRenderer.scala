package org.workcraft.gui.propertyeditor.bool

import java.awt.Component
import javax.swing.JCheckBox
import org.workcraft.gui.propertyeditor.RendererProvider

object BooleanCellRenderer extends RendererProvider[Boolean] {
  override def createRenderer(value: Boolean): Component = {
    val cb = new JCheckBox
    cb.setOpaque(false)
    cb.setFocusable(false)
    cb.setSelected(value)
    cb
  }
}
