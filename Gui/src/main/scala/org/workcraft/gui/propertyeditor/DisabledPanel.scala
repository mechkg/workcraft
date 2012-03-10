package org.workcraft.gui.propertyeditor
import javax.swing.JPanel
import java.awt.Font
import java.awt.Graphics
import java.awt.Color

class DisabledPanel extends JPanel {
	
  private val myfont = Font.getFont(Font.DIALOG)

	override def paint(g : Graphics) = {
		super.paint(g);
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);

		g.setFont(myfont);
		g.drawString("N/A", getWidth()/2, getHeight()/2);
	}
}
