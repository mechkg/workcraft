package org.workcraft.gui
import javax.swing.JMenu
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._
import javax.swing.JMenuItem
import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener
import javax.swing.JComponent

abstract class ReactiveMenu(title: String) extends JMenu(title) {
  def items: Expression[List[JComponent]]
  
  val refresh = swingAutoRefresh(items, (i: List[JComponent]) => ioPure.pure{
  
      removeAll()
      items.eval.unsafePerformIO.foreach(add(_))
      if (getItemCount() == 0) 
        setEnabled(false)
      else
        setEnabled(true)
  })
}