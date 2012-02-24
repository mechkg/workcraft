package org.workcraft.gui
import javax.swing.JMenu
import javax.swing.JMenuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.ModelDescriptorService

class FileMenu (services: GlobalServiceManager, mainWindow: MainWindow) extends JMenu ("File") {
  val newWork = new JMenuItem ("New work")
  setMnemonic('F')
  
  newWork.addActionListener(new ActionListener {
    def actionPerformed (e: ActionEvent) = {
      CreateWorkDialog.show(services.implementations(ModelDescriptorService), mainWindow)
    } 
  })
  
  newWork.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK))
  
  add(newWork)
}