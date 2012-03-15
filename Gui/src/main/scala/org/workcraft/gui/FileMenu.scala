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
import java.awt.EventDispatchThread
import javax.swing.event.MenuListener
import javax.swing.event.MenuEvent
import javax.swing.JSeparator

class FileMenu(services: () => GlobalServiceManager, mainWindow: MainWindow, newModel: ((NewModelImpl, Boolean)) => Unit) extends ReactiveMenu("File") {

  val items = mainWindow.editorInFocus.map(editor => {
    val newWork = menuItem("New work", Some('N'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)),
      CreateWorkDialog.show(services().implementations(NewModelService), mainWindow).foreach(newModel(_)))
      
    val open = menuItem("Open file...", Some('O'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)), OpenDialog.open(mainWindow, services()).foreach(mainWindow.openEditor(_)))

    val save = editor match {
      case Some(e) => {
        val save = menuItem("Save", Some('S'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)), SaveDialog.saveAs(mainWindow, e.content.model, services()).foreach (_.job.unsafePerformIO))
        val saveAs = menuItem("Save as...", Some('a'), None, SaveDialog.saveAs(mainWindow, e.content.model, services()).foreach(_.job.unsafePerformIO))
        List(save, saveAs)
      }
      case None => {
        val save = menuItem("Save", None, None, {})
        val saveAs = menuItem("Save as...", None, None, {})
        save.setEnabled(false)
        saveAs.setEnabled(false)
        List(save, saveAs)
      }
    }

    val exit = menuItem("Exit", Some('x'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)), mainWindow.exit)

    List(newWork, open) ++ save ++ List(new JSeparator(), exit)
  })

  setMnemonic('F')
}