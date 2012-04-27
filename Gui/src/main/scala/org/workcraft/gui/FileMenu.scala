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
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import org.workcraft.services.ExportError
import javax.swing.JOptionPane
import IOUtils._

class FileMenu(services: GlobalServiceManager, mainWindow: MainWindow, newModel: ((NewModelImpl, Boolean)) => IO[Unit]) extends ReactiveMenu("File") {
  def handleExportError(error: Option[ExportError]): IO[Unit] = error match {
    case None => IO.Empty
    case Some(error) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, error match { case ExportError.Exception(e) => e.toString(); case ExportError.Message(m) => m }, "Error", JOptionPane.ERROR_MESSAGE) }
  }

  def unsupportedFormatWarning (errorMessage: String): IO[Unit] = ioPure.pure {
    JOptionPane.showMessageDialog(mainWindow, 
				  "Workcraft is unable to save this file in its current format.\nPlease use the Save As command and save it using one of the supported formats.\n\nWorkcraft is unable to use the current format for the following reason:\n" + errorMessage, "Unsupported format", JOptionPane.WARNING_MESSAGE)
  }

  def doSaveAs(model: ModelServiceProvider): IO[Unit] = 
    SaveDialog.saveAs(mainWindow, model, services) >>= {
      case Some((file, job)) => job >>= {
	case Some(error) => handleExportError(Some(error))
	case None => mainWindow.fileMapping.update (model, Some(file))
      }
      case None => IO.Empty
    }

  // Must be lazy because Scala allows to read uninitialized values
  lazy val items = mainWindow.editorInFocus.map(editor => {
    val newWork = menuItem("New work", Some('N'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)),
      CreateWorkDialog.show(services.implementations(NewModelService), mainWindow) >>= { case Some(choice) => newModel(choice); case None => IO.Empty })

    val open = menuItem("Open file...", Some('O'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)),
			OpenDialog.open(mainWindow, services) >>= {
			  case Some((file, model)) => mainWindow.openEditor(model, Some(file));
			  case None => IO.Empty })

    val save = editor match {
      case Some(e) => {
        val save = menuItem("Save", Some('S'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)), 
			    mainWindow.fileMapping.lastSavedAs(e.content.model).eval >>= {
			      case Some(file) => guessFormatFromExtension(file) match {
				case Some(format) => IOUtils.export (e.content.model, format, file, services) match {
				  case Left(err) => unsupportedFormatWarning(err)
				  case Right(job) => job >>= handleExportError
				}
				case None => doSaveAs (e.content.model)
			      }
			      case None => doSaveAs (e.content.model)
			    })

        val saveAs = menuItem("Save as...", Some('a'), None, doSaveAs(e.content.model))
        List(save, saveAs)
      }
      case None => {
        val save = menuItem("Save", None, None, IO.Empty)
        val saveAs = menuItem("Save as...", None, None, IO.Empty)
        save.setEnabled(false)
        saveAs.setEnabled(false)
        List(save, saveAs)
      }
    }

    def disabledExportMenu = {
      val export = menuItem("Export...", None, None, IO.Empty)
      export.setEnabled(false)
      export
    }

    def exportMenu(model: ModelServiceProvider, ex: List[(Format, ExportJob)]) = ex match {
      case Nil => disabledExportMenu
      case x => {
        val menu = new JMenu("Export...")
        x.map({
          case (fmt, job) => menuItem(fmt.description + " (" + fmt.extension + ")", None, None, SaveDialog.export(mainWindow, model, fmt, job) >>= {
            case Some(job) => job >>= handleExportError
            case None => IO.Empty
          })
        }).foreach(menu.add(_))
        menu
      }
    }

    val export = editor match {
      case Some(e) => {
        val defaultFormat = e.content.model.implementation(DefaultFormatService)

        val exporters = services.implementations(ExporterService).map(exp => (exp.targetFormat, exp.export(e.content.model)))
          .flatMap({ case (format, Right(job)) if (defaultFormat.map(_ != format).getOrElse(true)) => Some((format, job)); case _ => None })

        exportMenu(e.content.model, exporters)
      }
      case None => disabledExportMenu
    }

    val exit = menuItem("Exit", Some('x'), Some(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)), mainWindow.exit)

    List(newWork, open) ++ save ++ List(export, new JSeparator(), exit)
  })

  setMnemonic('F')
}
