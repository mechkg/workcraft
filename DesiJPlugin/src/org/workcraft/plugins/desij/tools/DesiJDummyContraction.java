package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.desij.DecompositionResultHandler;
import org.workcraft.plugins.desij.DesiJPresetManager;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.workspace.WorkspaceEntry;

public class DesiJDummyContraction implements Tool {

	private final Framework framework;

	public DesiJDummyContraction(Framework framework) {
		this.framework = framework;
	}
	
	@Override
	public String getSection() {
		return "Dummy contraction";
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {
		final ExportJob stgExporter = Export.chooseBestExporter(framework.getPluginManager(), we.getModelEntry(), Format.STG);
		return new ToolJob() {
			
			@Override
			public void run() {
				framework.getTaskManager().queue(new DesiJTask(stgExporter, framework, DesiJPresetManager.DUMMY_REMOVAL.getSettings()), 
						"Execution of DesiJ", new DecompositionResultHandler(framework, we.getWorkspacePath(), true));
			}
		};
	}
	
	@Override
	public String getDisplayName() {
		return "Contract dummies (DesiJ)";
	}
}
