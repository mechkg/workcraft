/**
 * 
 */
package org.workcraft.plugins.petrify.tools;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.petrify.SynthesisResultHandler;
import org.workcraft.plugins.petrify.tasks.SynthesisTask;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.workspace.WorkspaceEntry;

/**
 * @author Dominic Wist
 * Petrify's Complex Gate Synthesis without Technology Mapping
 */
public class PetrifyComplexGateSynthesis implements Tool {

	private final Framework framework;

	public PetrifyComplexGateSynthesis(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getSection() {
		return "Synthesis";
	}

	@Override
	public ToolJob applyTo(WorkspaceEntry we) throws ServiceNotAvailableException {
		ServiceProvider services = we.getModelEntry();
		final ExportJob dotGExportJob = Export.chooseBestExporter(framework.getPluginManager(), services, Format.STG);
		
		return new ToolJob(){

			@Override
			public void run() {
				//Custom button text
				Object[] options = {"Yes, please",
				                    "No, thanks",
				                    "Cancel Logic Synthesis"};
				int option = JOptionPane.showOptionDialog(framework.getMainWindow(),
				    "Would you like to do technology mapping as well?",
				    "Technology mapping",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null, // no icon
				    options,
				    options[2]); // initial Value: Cancel
				
				if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
					return;
				
				File libraryFile = null;
							
				if (option == JOptionPane.YES_OPTION) {
					// choose library File
					JFileChooser fc = new JFileChooser();
					fc.setDialogTitle("Choose a library file");
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY); // default
					if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
						libraryFile = fc.getSelectedFile();
				}
				
				// call petrify asynchronous (w/o blocking the GUI) 
				try {
					framework.getTaskManager().queue(
							new SynthesisTask(getComplexGateSynParamter(), getInputSTG(framework, dotGExportJob), 
							File.createTempFile("petrifyEquations", ".eqn"), libraryFile, null), 
							"Petrify Logic Synthesis", new SynthesisResultHandler(framework));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	
	private String[] getComplexGateSynParamter() {
		String[] result = new String[1];
		result[0] = "-cg";
		return result;
	}

	private File getInputSTG(Framework framework, ExportJob dotGExportJob) {
		File stgFile;
		
		try {
			stgFile = File.createTempFile("STG", ".g");
			ExportTask exportTask = new ExportTask(dotGExportJob, stgFile);
			framework.getTaskManager().execute(exportTask, "Exporting .g");
			
			return stgFile;
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDisplayName() {
		return "Complex gate synthesis (Petrify)";
	}

}
