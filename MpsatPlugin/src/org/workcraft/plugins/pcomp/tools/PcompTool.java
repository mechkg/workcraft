package org.workcraft.plugins.pcomp.tools;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.pcomp.gui.PcompDialog;
import org.workcraft.plugins.pcomp.tasks.PcompResultHandler;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.DotGFile;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class PcompTool implements Tool {

	private final Framework framework;

	public PcompTool(final Framework framework) {
		this.framework = framework;
	}

	@Override
	public final String getSection() {
		return "Composition";
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {
		return new ToolJob() {

			@Override
			public void run() {
				final PcompDialog dialog = new PcompDialog(framework.getMainWindow(), framework);
				GUI.centerAndSizeToParent(dialog, framework.getMainWindow());

				if (dialog.run()) {

					final ArrayList<File> inputs = new ArrayList<File>();

					for (final DotGFile p : dialog.getSelectedItems()) {
						inputs.add(p.getFile());
					}

					framework.getTaskManager().queue(new PcompTask(inputs.toArray(new File[0]), dialog.getMode(), dialog.isImprovedPcompChecked()),
							"Running pcomp", new PcompResultHandler(framework, dialog.showInEditor()));
				}
			}
		};
	}

	@Override
	public String getDisplayName() {
		return "Parallel composition (PComp)";
	}
}