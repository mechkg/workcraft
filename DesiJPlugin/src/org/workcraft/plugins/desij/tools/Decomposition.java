package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.desij.DecompositionResultHandler;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.workspace.WorkspaceEntry;

public class Decomposition implements Tool {

	private final Framework framework;

	public Decomposition(Framework framework) {
		this.framework = framework;
	}
	
	@Override
	public String getSection() {
		return "Decomposition";
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {
		
		final ExportJob stgExporter = Export.chooseBestExporter(framework.getPluginManager(), we.getModelEntry(), Format.STG);
		return new ToolJob(){

			@Override
			public void run() {
				// call desiJ asynchronous (w/o blocking the GUI)
				framework.getTaskManager().queue(new DesiJTask(stgExporter, framework, new String[0]), 
						"Execution of DesiJ", new DecompositionResultHandler(framework, we.getWorkspacePath(), true));
			}
		};
	}

	@Override
	public String getDisplayName() {
		return "Standard decomposition (DesiJ)";
	}
}
