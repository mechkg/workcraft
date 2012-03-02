package org.workcraft.gui
import javax.swing.JPanel
import java.awt.Font
import java.awt.Graphics
import java.awt.Color

class NotAvailablePanel extends JPanel {
  val labelFont = Font.getFont(Font.DIALOG)

  override def paint(g: Graphics) = {
    super.paint(g)
    g.setColor(Color.LIGHT_GRAY)
    g.drawRect(0, 0, getWidth() - 1, getHeight() - 1)

    g.setFont(labelFont);
    g.drawString("N/A", getWidth() / 2, getHeight() / 2)
  }
}