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
import org.workcraft.services.ExporterService
import org.workcraft.services.ExportJob
import org.workcraft.services.Format
import org.workcraft.services.DefaultFormatService

class EditMenu(mainWindow: MainWindow) extends ReactiveMenu("Edit") {
  
  // Must be lazy because Scala allows to read uninitialized values
  lazy val items = mainWindow.editorInFocus.map(_.flatMap(_.content.editor.undo) match {
    case Some(undo) => {
      List(undo.undo.unsafeEval match {
        case Some(action) => menuItem("Undo: " + action.description, Some('U'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK)), { action.action.unsafePerformIO })
        case None => menuItem("Nothing to undo", None, None, {})
      })
    }
    case None => {
      List()
    }
  })

  setMnemonic('E')
}