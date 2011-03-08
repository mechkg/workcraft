package org.workcraft.plugins.petrify.tasks;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyDummyContractionResultHandler extends DummyProgressMonitor<PetrifyDummyContractionResult> {
	private final Framework framework;
	private final Path<String> path;

	public PetrifyDummyContractionResultHandler(Framework framework, Path<String> path) {
		this.framework = framework;
		this.path = path;
	}

	@Override
	public void finished(final Result<? extends PetrifyDummyContractionResult> result, String description) {

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run() {
				String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
				
				if (result.getOutcome() == Outcome.FINISHED)
				{
					ServiceProvider model = result.getReturnValue().getResult();
					final WorkspaceEntry resolved = framework.getWorkspace().add(path.getParent(), fileName + "_contracted", model, true);
					framework.getMainWindow().createEditorWindow(resolved);
				} else
				{
					if (result.getCause() == null)
						JOptionPane.showMessageDialog(framework.getMainWindow(), "Petrify output: \n\n" + new String(result.getReturnValue().getPetrifyResult().getReturnValue().getErrors()), "Dummy contraction failed", JOptionPane.WARNING_MESSAGE);
					else
						ExceptionDialog.show(framework.getMainWindow(), result.getCause());
				}
			}

		});
	}
}
