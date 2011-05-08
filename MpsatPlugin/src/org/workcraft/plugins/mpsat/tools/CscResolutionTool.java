package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.workspace.WorkspaceEntry;

public class CscResolutionTool implements Tool {

	private final Framework framework;

	public CscResolutionTool(final Framework framework) {
		this.framework = framework;
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {

		final MpsatSettings settings = new MpsatSettings(MpsatMode.RESOLVE_ENCODING_CONFLICTS, 4, MpsatSettings.SOLVER_MINISAT, SolutionMode.MINIMUM_COST, 1, null);
		final MpsatChainTask mpsatTask = MpsatChainTask.create(we, settings, framework);

		return new ToolJob() {
			@Override
			public void run() {
				framework.getTaskManager().queue(mpsatTask, "CSC conflicts resolution", new MpsatChainResultHandler(framework, we));
			}
		};
	}

	@Override
	public String getSection() {
		return "Encoding conflicts";
	}

	@Override
	public String getDisplayName() {
		return "Resolve CSC conflicts";
	}
}