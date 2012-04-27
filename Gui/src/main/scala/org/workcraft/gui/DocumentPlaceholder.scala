package org.workcraft.gui

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.IOException
import javax.swing.JPanel

class DocumentPlaceholder (background: Color) extends JPanel {
	lazy val logoImage = GUI.loadImageFromResource("images/logo.png")

	setBackground(background)
	setLayout(null)
	
	override def paint(g: Graphics) = {
		super.paint(g)
		
		logoImage match {
		  case Right(image) => {
			val w = image.getWidth()
			val h = image.getHeight()
		
			val x = (getWidth() - w)/2;
			val y = (getHeight() - h)/2;
		
			g.drawImage(image, x, y, null)
		  }
		  case Left(_) => {}
		}
	}
}
