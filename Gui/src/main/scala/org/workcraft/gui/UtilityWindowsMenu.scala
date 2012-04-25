package org.workcraft.gui
import org.workcraft.gui.docking.DockableWindow
import javax.swing.JMenu
import javax.swing.JCheckBoxMenuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.JComponent
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._

class UtilityWindowMenuItem(window: DockableWindow[_]) extends JCheckBoxMenuItem {
  setText(window.title.unsafeEval)

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

  //FIXME: utility window title is constant, but this is not reflected in type Expression
  //       explicit constant type should be introduced to handle such cases
  utilityWindows.sortBy(_.title.unsafeEval).foreach(w => add(menuItems(w)))

  def update(window: DockableWindow[_ <: JComponent]) = menuItems.get(window).foreach(_.setSelected(!window.isClosed))
  
  setMnemonic('W')
}
