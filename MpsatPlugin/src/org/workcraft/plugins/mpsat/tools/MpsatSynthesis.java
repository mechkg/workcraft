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

public class MpsatSynthesis implements Tool {

	private final Framework framework;

	public MpsatSynthesis(final Framework framework) {
		this.framework = framework;
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {
		final MpsatSettings settings = new MpsatSettings(MpsatMode.COMPLEX_GATE_IMPLEMENTATION, 0, MpsatSettings.SOLVER_MINISAT, SolutionMode.FIRST, 1, null);
		final MpsatChainTask task = MpsatChainTask.create(we, settings, framework);
		return new ToolJob() {

			@Override
			public void run() {
				framework.getTaskManager().queue(task, "Complex gate synthesis with MPSat", new MpsatChainResultHandler(framework, we));
			}
		};
	}

	@Override
	public String getSection() {
		return "Synthesis";
	}

	@Override
	public String getDisplayName() {
		return "Complex gate synthesis (MPSat)";
	}

}
