package org.workcraft.gui

import javax.swing.JMenuBar
import javax.swing.JMenu
import javax.swing.JMenuItem
import org.workcraft.gui.docking.DockableWindow
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.NewModelImpl
import javax.swing.JComponent

import org.workcraft.scala.effects.IO

class MainMenu(
  mainWindow: MainWindow,
  utilityWindows: List[DockableWindow[_ <: JComponent]], 
  services: GlobalServiceManager,
  newModel: ((NewModelImpl, Boolean)) => IO[Unit]
) extends JMenuBar {
  val fileMenu = new FileMenu(services, mainWindow, newModel)
  val editMenu = new EditMenu(mainWindow)
  val windowsMenu = new UtilityWindowsMenu(utilityWindows)
  val toolsMenu = new ToolsMenu(services, mainWindow)
  val aboutMenu = new AboutMenu(mainWindow)
  
  add(fileMenu)
  add(editMenu)
  add(windowsMenu)
  add(toolsMenu)
  add(aboutMenu)

}
