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

object SaveDialog {
  def show(parentWindow: Window, file: Option[File], model: ModelServiceProvider, globalServices: GlobalServiceManager) = {
    val exporters = globalServices.implementations(ExporterService).flatMap(e => e.export(model) match {
      case Left(_) => None
      case Right(job) => Some((e.targetFormat, job))
    })

    if (exporters.isEmpty)
      JOptionPane.showMessageDialog(parentWindow, "Workcraft was unable to find any plug-ins that could save this model.", "Warning", JOptionPane.WARNING_MESSAGE)
    else {
      val fc = new JFileChooser()
      fc.setDialogType(JFileChooser.SAVE_DIALOG)

      exporters.foreach({
        case (fmt, _) => fc.addChoosableFileFilter(new FileFilter {
          def accept(file: File) = file.getName().endsWith(fmt.extension)
          def getDescription: String = fmt.description
        })
      })

      fc.setAcceptAllFileFilterUsed(false)

      file.foreach(f => fc.setCurrentDirectory(f.getParentFile))

      def choose: Option[File] = if (fc.showSaveDialog(parentWindow) == JFileChooser.APPROVE_OPTION) {
        var path = fc.getSelectedFile().getPath()

        println(fc.getFileFilter())

        /*if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION))
					path += FileFilters.DOCUMENT_EXTENSION; */

        val f = new File(path);

        if (!f.exists())
          Some(f)
        else if (JOptionPane.showConfirmDialog(parentWindow, "The file \"" + f.getName() + "\" already exists. Do you want to overwrite it?", "Confirm",
          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
          Some(f)
        else
          choose
      } else
        None
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
  }
}