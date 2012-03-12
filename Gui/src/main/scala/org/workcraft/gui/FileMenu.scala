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
import org.workcraft.services.ModelServiceProvider
import java.awt.event.MouseListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.Component
import javax.swing.JPopupMenu

class FileMenu(services: () => GlobalServiceManager, mainWindow: MainWindow, newModel: ((NewModelImpl, Boolean)) => Unit) extends JMenu("File") {
  override def getPopupMenu = {
    println ("gavnooo")
    
    val menu = new JPopupMenu()
    menu.add (new JMenuItem (scala.math.random.toString))
    menu.add(menuItem("New work", Some('N'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)),
      CreateWorkDialog.show(services().implementations(NewModelService), mainWindow).foreach(newModel(_))))

    menu.add(menuItem("Save as...", Some('a'), None, SaveDialog.show(mainWindow, None, mainWindow.editorInFocus.map(_.content.model).getOrElse(ModelServiceProvider.Empty), services())))

    menu.addSeparator

    menu.add(menuItem("Exit", Some('x'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)),
      () => mainWindow.exit))
   
    menu.setVisible(true)
      
    menu
  }
  
  setMnemonic('F')

}