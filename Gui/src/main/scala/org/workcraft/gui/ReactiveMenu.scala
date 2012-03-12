package org.workcraft.gui
import javax.swing.JMenu
import org.workcraft.scala.Expressions._
import javax.swing.JMenuItem
import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener
import javax.swing.JComponent

abstract class ReactiveMenu(title: String) extends JMenu(title) {
  val items: Expression[List[JComponent]] 
  
  addMenuListener (new MenuListener {
    def menuCanceled(e: MenuEvent) = {}
    def menuSelected(e: MenuEvent) = {
      removeAll()
      items.eval.unsafePerformIO.foreach(add(_))
    }
    def menuDeselected (e: MenuEvent) = {}
  })      
}