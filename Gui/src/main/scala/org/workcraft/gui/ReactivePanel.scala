package org.workcraft.gui
import javax.swing.JPanel
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._
import java.awt.BorderLayout

abstract class ReactivePanel extends JPanel {
  def contents: Expression[JPanel]
  
  val container = new JPanel(new BorderLayout)
  
  val refresh = swingAutoRefresh(contents, (panel: JPanel) => ioPure.pure {
    container.removeAll()
    container.add(panel, BorderLayout.CENTER)
  })
}