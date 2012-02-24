package org.workcraft.gui

import javax.swing.JMenuBar
import javax.swing.JMenu
import javax.swing.JMenuItem
import org.workcraft.gui.docking.DockableWindow
import org.workcraft.services.GlobalServiceManager

class MainMenu(mainWindow: MainWindow, utilityWindows: List[DockableWindow], services: GlobalServiceManager) extends JMenuBar {
  val windowsMenu = new UtilityWindowsMenu(utilityWindows)
  val fileMenu = new FileMenu(services, mainWindow)
  add(fileMenu)
  add(windowsMenu)
}