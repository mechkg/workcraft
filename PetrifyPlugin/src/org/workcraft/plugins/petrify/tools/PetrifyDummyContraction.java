package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.petrify.tasks.PetrifyDummyContractionResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyDummyContractionTask;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyDummyContraction implements Tool {
	private Framework framework;
	
	public PetrifyDummyContraction(Framework framework) {
		this.framework = framework;
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {
		final PetrifyDummyContractionTask task = new PetrifyDummyContractionTask(framework, we);
		return new ToolJob(){
			@Override
			public void run() {
				framework.getTaskManager().queue(task, "Petrify dummy contraction", new PetrifyDummyContractionResultHandler(framework, we.getWorkspacePath()));
			}
			
		};
	}
	
	@Override
	public String getSection() {
		return "Dummy contraction";
	}

	@Override
	public String getDisplayName() {
		return "Contract dummies (Petrify)";
	}
}
