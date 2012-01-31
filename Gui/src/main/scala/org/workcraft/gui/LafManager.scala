package org.workcraft.gui

import javax.swing.UIManager
import org.workcraft.logging.Logger
import org.workcraft.logging.Logger._
import scalaz.effects.IO

object LafManager {
	private var currentLaf = UIManager.getLookAndFeel().getClass().getName()

	def getCurrentLaf = currentLaf

	def setLaf(laf: String)(implicit logger: Logger[IO]) = {
		try {
		    info("Setting LaF: " + laf).unsafePerformIO
			UIManager.setLookAndFeel(laf)
			currentLaf = laf;
		} catch {
		  case e => warning(e).unsafePerformIO
		} 
	}

	def setDefaultLaf (implicit logger: Logger[IO]) = setLaf(UIManager.getCrossPlatformLookAndFeelClassName())
}