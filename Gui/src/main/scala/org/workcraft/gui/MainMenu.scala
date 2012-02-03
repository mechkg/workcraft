package org.workcraft.gui

import javax.swing.JMenuBar
import javax.swing.JMenu
import javax.swing.JMenuItem
import org.workcraft.gui.docking.DockableWindow

class MainMenu(val utilityWindows: List[DockableWindow]) extends JMenuBar {
  val windowsMenu = new UtilityWindowsMenu(utilityWindows)
  add(windowsMenu)
}