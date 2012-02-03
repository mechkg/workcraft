package org.workcraft.gui
import org.workcraft.gui.docking.DockableWindow
import javax.swing.JMenu
import javax.swing.JCheckBoxMenuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent

class UtilityWindowMenuItem(window: DockableWindow) extends JCheckBoxMenuItem {
  setText(window.title)
  setSelected(!window.isClosed)

  addActionListener(new ActionListener() {
    override def actionPerformed(evt: ActionEvent) = {
      if (window.isClosed)
        window.display
      else
        window.close
      setSelected(!window.isClosed)
    }
  })
}

class UtilityWindowsMenu(val utilityWindows: List[DockableWindow]) extends JMenu("Windows") {
  val menuItems = utilityWindows.map(w => (w, new UtilityWindowMenuItem(w))).toMap
  utilityWindows.sortBy(_.title).foreach(w => add(menuItems(w)))

  def update(window: DockableWindow) = menuItems.get(window).foreach(_.setSelected(!window.isClosed))
}