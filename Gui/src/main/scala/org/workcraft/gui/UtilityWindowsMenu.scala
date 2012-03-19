package org.workcraft.gui
import org.workcraft.gui.docking.DockableWindow
import javax.swing.JMenu
import javax.swing.JCheckBoxMenuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.JComponent

class UtilityWindowMenuItem(window: DockableWindow[_]) extends JCheckBoxMenuItem {
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

class UtilityWindowsMenu(val utilityWindows: List[DockableWindow[_ <: JComponent]]) extends JMenu("Windows") {
  val menuItems: Map[DockableWindow[_ <: JComponent], UtilityWindowMenuItem]= utilityWindows.map(w => (w, new UtilityWindowMenuItem(w))).toMap
  utilityWindows.sortBy(_.title).foreach(w => add(menuItems(w)))

  def update(window: DockableWindow[_ <: JComponent]) = menuItems.get(window).foreach(_.setSelected(!window.isClosed))
  
  setMnemonic('W')
}