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

object OpenDialog {
  def chooseFile(currentFile: Option[File], parentWindow: Window, formats: List[Format]): Option[File] = {
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

  def openFile(parentWindow: Window, file: File, importers: List[FileOpen]): Option[FileOpenJob] = importers.flatMap(_.open(file).unsafePerformIO) match {
    case Nil => {
      JOptionPane.showMessageDialog(parentWindow, "No import plug-ins could recognise this file.", "Error", JOptionPane.ERROR_MESSAGE)
      None
    }
    case one :: Nil => Some(one)
    case many => JOptionPane.showInputDialog(parentWindow, "More that one plug-in is able to open this file.\nPlease choose which plug-in to use:", "Choice",
      JOptionPane.QUESTION_MESSAGE, null, many.toArray, many.head) match {
        case job: FileOpenJob => Some(job)
        case _ => None
      }
  }

  def open(parentWindow: Window, globalServices: GlobalServiceManager): Option[ModelServiceProvider] = {
    val importers = globalServices.implementations(FileOpenService)

    if (importers.isEmpty) {
      JOptionPane.showMessageDialog(parentWindow, "No import plug-ins are available.", "Error", JOptionPane.ERROR_MESSAGE)
      None
    } else

      chooseFile(None, parentWindow, importers.map(_.sourceFormat).distinct) match {
        case Some(file) => openFile(parentWindow, file, importers).flatMap(_.job.unsafePerformIO match {
          case Left(error) => {
            val label = new JTextArea(error)
            label.setEditable(false)
            
            label.setFont(new Font("Monospaced", 0, 12))
            JOptionPane.showMessageDialog(parentWindow, label, "Error", JOptionPane.ERROR_MESSAGE); None
          }
          case Right(model) => Some(model)
        })
        case None => None
      }
  }
}