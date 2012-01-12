package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.ToolJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractMpsatChecker {

	private final Framework framework;

	public AbstractMpsatChecker(final Framework framework) {
		this.framework = framework;
	}

	public final String getSection() {
		return "Verification";
	}

	protected abstract MpsatSettings getSettings();

	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {

		final MpsatChainTask mpsatTask = MpsatChainTask.create(we, getSettings(), framework);

		return new ToolJob() {

			@Override
			public void run() {
				final String title = we.getTitle();
				String description = "MPSat tool chain";
				if (!title.isEmpty())
					description += "(" + title + ")";

				framework.getTaskManager().queue(mpsatTask, description, new MpsatChainResultHandler(framework, we));
			}
		};
	}
}
