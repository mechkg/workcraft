package org.workcraft.gui

import javax.swing.UIManager
import org.workcraft.logging.Logger
import org.workcraft.logging.Logger._
import org.workcraft.scala.effects.IO
import javax.swing.SwingUtilities

object LafManager {
  private var currentLaf = UIManager.getLookAndFeel().getClass().getName()

  def getCurrentLaf = currentLaf

  def setLaf(laf: String)(implicit logger: () => Logger[IO]) = {
    unsafeInfo("Setting LaF: " + laf)
    try {
      SwingUtilities.invokeAndWait(new Runnable {
        def run = UIManager.setLookAndFeel(laf)
      })
      currentLaf = laf;
    } catch {
      case e => warning(e).unsafePerformIO
    }
  }

  def setDefaultLaf(implicit logger: () => Logger[IO]) = setLaf(UIManager.getCrossPlatformLookAndFeelClassName())
}