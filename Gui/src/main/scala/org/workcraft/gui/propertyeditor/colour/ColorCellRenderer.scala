package org.workcraft.gui.propertyeditor.colour

import java.awt.Color
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JLabel
import org.workcraft.gui.propertyeditor.RendererProvider

object ColorCellRenderer extends RendererProvider[Color] {
  private val isBordered: Boolean = true

  override def createRenderer(value: Color): Component = {
    val label: JLabel = new JLabel
    label.setOpaque(true); //MUST do this for background to show up.
    label.setFocusable(false);

    label.setBackground(value);
    if (isBordered) {
      val borderBackground = Color.WHITE; // TODO: label.isSelected ? table.getSelectionBackground() : table.getBackground()
      label.setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, borderBackground));
    }

    return label;
  }
}
