package org.workcraft.gui
import javax.swing.JMenu
import javax.swing.JMenuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.NewModelService
import GUI.menuItem
import org.workcraft.services.NewModelImpl

class FileMenu (services: () => GlobalServiceManager, mainWindow: MainWindow, newModel: NewModelImpl => Unit) extends JMenu ("File") {
  setMnemonic('F')
  
  add(menuItem ("New work", Some('N'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)), 
      () => CreateWorkDialog.show(services().implementations(NewModelService), mainWindow).foreach(newModel(_))))

  addSeparator
  
  add(menuItem ("Exit", Some('x'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)),
      () => mainWindow.exit))
}