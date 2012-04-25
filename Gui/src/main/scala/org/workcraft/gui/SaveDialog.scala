package org.workcraft.gui
import javax.swing.JFileChooser
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.GlobalServiceProvider
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.ExporterService
import javax.swing.JOptionPane
import java.awt.Window
import javax.swing.filechooser.FileFilter
import java.io.File
import org.workcraft.services.DefaultFormatService
import org.workcraft.services.Format
import org.workcraft.services.ExportJob
import java.io.FileOutputStream
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._
import org.workcraft.services.ExportError

object SaveDialog {

  // Save        }
  // Save as...  } use default format
  // 
  // Export...   } choose format

  def partitionEither[A, B](list: List[Either[A, B]]) =
    list.foldRight((List[A](), List[B]()))((item, lists) => item match {
      case Left(left) => (left :: lists._1, lists._2)
      case Right(right) => (lists._1, right :: lists._2)
    })

  def chooseFile(currentFile: Option[File], parentWindow: Window, format: Format): IO[Option[File]] = ioPure.pure {
    val fc = new JFileChooser()
    fc.setDialogType(JFileChooser.SAVE_DIALOG)

    fc.setFileFilter(new FileFilter {
      def accept(file: File) = file.isDirectory || file.getName.endsWith(format.extension)
      def getDescription = format.description + "(" + format.extension + ")"
    })
    fc.setAcceptAllFileFilterUsed(false)

    currentFile.foreach(f => fc.setCurrentDirectory(f.getParentFile))

    def choose: Option[File] = if (fc.showSaveDialog(parentWindow) == JFileChooser.APPROVE_OPTION) {
      var path = fc.getSelectedFile().getPath()

      if (!path.endsWith(format.extension))
        path += format.extension

      val f = new File(path)

      if (!f.exists())
        Some(f)
      else if (JOptionPane.showConfirmDialog(parentWindow, "The file \"" + f.getName() + "\" already exists. Do you want to overwrite it?", "Confirm",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        Some(f)
      else
        choose
    } else
      None

    choose
  }

  def export(parentWindow: Window, model: ModelServiceProvider, format: Format, exporter: ExportJob): IO[Option[IO[Option[ExportError]]]] = chooseFile (None, parentWindow, format).map(_.map(exporter.job(_)))

  def saveAs(parentWindow: Window, model: ModelServiceProvider, globalServices: GlobalServiceManager): IO[Option[(File, IO[Option[ExportError]])]] = model.implementation(DefaultFormatService) match {
    case None => ioPure.pure {
      JOptionPane.showMessageDialog(parentWindow, "Current model does not define a default file format.\nTry using export and choosing a specific format instead.", "Error", JOptionPane.ERROR_MESSAGE)
      None
    }

    case Some(format) => {
      val exporters = globalServices.implementations(ExporterService).filter(_.targetFormat == format)
      val (unapplicable, applicable) = partitionEither(exporters.map(_.export(model)))

      if (applicable.isEmpty) {
        val explanation = if (exporters.isEmpty)
          "Because no export plug-ins are available for this format."
        else
          "Because:\n" + unapplicable.map("- " + _.toString).reduceRight(_ + "\n" + _)

        ioPure.pure {
          JOptionPane.showMessageDialog(parentWindow,
            "Workcraft was unable to save this model in its default format:\n" + format.description + " (" + format.extension + ")\n" + explanation, "Error", JOptionPane.ERROR_MESSAGE)
          None
        }

      } else
        // TODO: handle more than one exporter
        chooseFile(None, parentWindow, format).map(_.map( f => (f, applicable.head.job(f))))
    }
  }
}     

    /*
	


		

		String path;


		try {
			
			File destination = new File(path);
			Workspace ws = framework.getWorkspace();
			
			final Path<String> wsFrom = we.getWorkspacePath();
			Path<String> wsTo = ws.getWorkspacePath(destination);
			if(wsTo == null)
				wsTo = ws.tempMountExternalFile(destination);
			ws.moved(wsFrom, wsTo);

			if (we.getModelEntry() != null)
				framework.save(we.getModelEntry(), we.getFile().getPath());
			else
				throw new RuntimeException ("Cannot save workspace entry - it does not have an associated Workcraft model.");
			lastSavePath = fc.getCurrentDirectory().getPath();
		} catch (SerialisationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Model export failed", JOptionPane.ERROR_MESSAGE);			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
  }*/
 
