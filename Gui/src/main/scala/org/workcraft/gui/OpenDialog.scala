package org.workcraft.gui
import java.awt.Window
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.FileOpenService
import java.io.File
import javax.swing.JFileChooser
import org.workcraft.services.FileOpen
import javax.swing.filechooser.FileFilter
import org.workcraft.scala.effects.IO
import org.workcraft.services.Format
import org.workcraft.services.FileOpenJob
import javax.swing.JOptionPane
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JTextArea

import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._

object OpenDialog {
  def chooseFile(currentFile: Option[File], parentWindow: Window, formats: List[Format]): IO[Option[File]] = ioPure.pure {
    val fc = new JFileChooser()
    fc.setDialogType(JFileChooser.OPEN_DIALOG)

    formats.foreach(fmt => fc.addChoosableFileFilter(new FileFilter {
      def accept(file: File) = file.isDirectory || file.getName.endsWith(fmt.extension)
      def getDescription = fmt.description + "(" + fmt.extension + ")"
    }))
    fc.setAcceptAllFileFilterUsed(false)

    currentFile.foreach(f => fc.setCurrentDirectory(f.getParentFile))

    if (fc.showOpenDialog(parentWindow) == JFileChooser.APPROVE_OPTION)
      Some(fc.getSelectedFile())
    else
      None
  }

  def openFile(parentWindow: Window, file: File, importers: List[FileOpen]): IO[Option[FileOpenJob]] = importers.map(_.open(file)).sequence >>= (_.flatten match {
    case Nil => ioPure.pure {
      JOptionPane.showMessageDialog(parentWindow, "No import plug-in could recognise this file.", "Cannot open file", JOptionPane.ERROR_MESSAGE)
      None
    }
    case one :: Nil => ioPure.pure { Some(one) }
    case many => ioPure.pure {
      JOptionPane.showInputDialog(parentWindow, "More that one plug-in is able to open this file.\nPlease choose which plug-in to use:", "Choice",
        JOptionPane.QUESTION_MESSAGE, null, many.toArray, many.head) match {
          case job: FileOpenJob => Some(job)
          case _ => None
        }
    }
  })

  def open(parentWindow: Window, globalServices: GlobalServiceManager): IO[Option[(File, ModelServiceProvider)]] = {
    val importers = globalServices.implementations(FileOpenService)

    if (importers.isEmpty) ioPure.pure {
      JOptionPane.showMessageDialog(parentWindow, "No import plug-ins are available.", "Error", JOptionPane.ERROR_MESSAGE)
      None
    }
    else
      chooseFile(None, parentWindow, importers.map(_.sourceFormat).distinct) >>= {
        case Some(file) => openFile(parentWindow, file, importers) >>= {
          case Some(openJob) => openJob.job >>= {
            case Left(error) => ioPure.pure {
              val label = new JTextArea(error)
              label.setEditable(false)

              label.setFont(new Font("Monospaced", 0, 12))
              JOptionPane.showMessageDialog(parentWindow, label, "Error", JOptionPane.ERROR_MESSAGE)

              None
            }
            case Right(model) => ioPure.pure { Some((file, model))  }
          }
          case None => ioPure.pure { None }
        }
        case None => ioPure.pure { None }
      }
  }
}
