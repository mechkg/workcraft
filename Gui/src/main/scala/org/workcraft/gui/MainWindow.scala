package org.workcraft.gui

import java.awt.Font
import javax.swing.JFrame
import org.workcraft.services.GlobalServiceManager
import org.workcraft.logging.Logger

class MainWindow (val globalServices : GlobalServiceManager) extends JFrame {
  setTitle ("Workcraft")
  setSize (800,600)
}