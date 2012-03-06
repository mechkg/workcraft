package org.workcraft.gui

import javax.swing.JMenuBar
import javax.swing.JMenu
import javax.swing.JMenuItem
import org.workcraft.gui.docking.DockableWindow
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.NewModelImpl
import javax.swing.JComponent

class MainMenu(
    mainWindow: MainWindow, utilityWindows: List[DockableWindow[_ <: JComponent]], 
    services: () => GlobalServiceManager,
    newModel: ((NewModelImpl, Boolean)) => Unit,
    reconfigure: () => Unit) extends JMenuBar {
  val fileMenu = new FileMenu(services, mainWindow, newModel)
  val windowsMenu = new UtilityWindowsMenu(utilityWindows)
  
  val utilityMenu = new JMenu("Utility")
  utilityMenu.setMnemonic('U')
  utilityMenu.add(GUI.menuItem("Reconfigure", Some('R'), None, reconfigure))
  
  add(fileMenu)
  add(windowsMenu)
  add(utilityMenu)
}