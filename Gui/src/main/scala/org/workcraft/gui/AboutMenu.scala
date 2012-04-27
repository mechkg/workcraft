package org.workcraft.gui
import java.awt.Component
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JOptionPane
import org.workcraft.gui.docking.DockableWindow
import javax.swing.JMenu
import GUI.menuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.JComponent
import javax.swing.JPanel
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._

class AboutMenu (mainWindow: MainWindow) extends JMenu("About") {
  val panel = new JPanel()
  panel.setLayout (new BoxLayout(panel, BoxLayout.PAGE_AXIS))
  panel.add (new DocumentPlaceholder (panel.getBackground))

  val label1 = new JLabel ("(c) 2006-2011 Newcastle University")
  label1.setAlignmentX(Component.CENTER_ALIGNMENT)

  val label2 = new JLabel ("Written by Ivan Poliakov and Arseniy Alekseyev")
  label2.setAlignmentX(Component.CENTER_ALIGNMENT)

  val label3 = new JLabel ("http://workcraft.org")
  label3.setAlignmentX(Component.CENTER_ALIGNMENT)

  val label4 = new JLabel ("Special thanks to:")
  val label5 = new JLabel ("Alex Yakovlev, Victor Khomenko, Danil Sokolov, Andrey Mokhov,")
  val label6 = new JLabel ("Stanislavs Golubcovs and Ashur Rafiev")

  label4.setAlignmentX(Component.CENTER_ALIGNMENT)
  label5.setAlignmentX(Component.CENTER_ALIGNMENT)
  label6.setAlignmentX(Component.CENTER_ALIGNMENT)

  panel.add (label3)
  panel.add (label1)
  panel.add (label2)
  panel.add (label4)
  panel.add (label5)
  panel.add (label6)

  panel.setPreferredSize (new Dimension (600, 250))

  val item = menuItem("About Workcraft", None, None, ioPure.pure {
    JOptionPane.showMessageDialog (mainWindow, panel, "About Workcraft", JOptionPane.PLAIN_MESSAGE, null)
  })

  add (item)  
  setMnemonic('A')
}
