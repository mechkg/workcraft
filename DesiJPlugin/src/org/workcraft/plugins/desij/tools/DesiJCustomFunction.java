package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.desij.DecompositionResultHandler;
import org.workcraft.plugins.desij.DesiJPresetManager;
import org.workcraft.plugins.desij.gui.DesiJConfigurationDialog;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class DesiJCustomFunction implements Tool {

	private final Framework framework;

	public DesiJCustomFunction(Framework framework){
		this.framework = framework;
	}
	
	@Override
	public String getSection() {
		return "Decomposition";
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {
		final ExportJob stgExporter = Export.chooseBestExporter(framework.getPluginManager(), we.getModelEntry(), Format.STG);

		return new ToolJob() {
			@Override
			public void run() {
				DesiJPresetManager pmgr = new DesiJPresetManager();
				DesiJConfigurationDialog dialog = new DesiJConfigurationDialog(framework.getMainWindow(), pmgr);
				GUI.centerAndSizeToParent(dialog, framework.getMainWindow());
				dialog.setVisible(true);
				if (dialog.getModalResult() == 1)
				{
					framework.getTaskManager().queue(new DesiJTask(stgExporter, framework, dialog.getSettings()), 
							"DesiJ Execution", new DecompositionResultHandler(framework, we.getWorkspacePath(), false));
				}
			}
		};
	}

	@Override
	public String getDisplayName() {
		return "Customised function (DesiJ)";
	}
}