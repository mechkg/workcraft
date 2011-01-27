package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCscResolutionResultHandler implements Runnable {

	private final MpsatChainTask task;
	private final Result<? extends MpsatChainResult> mpsatChainResult;
	private final StorageManager storage;

	public MpsatCscResolutionResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> mpsatChainResult, StorageManager storage) {
				this.task = task;
				this.mpsatChainResult = mpsatChainResult;
				this.storage = storage;
	}
	
	public STGModel getResolvedStg()
	{
		final byte[] output = mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getOutputFile("mpsat.g");
		if(output == null)
			return null;
		
		try {
			return new DotGImporter().importSTG(new ByteArrayInputStream(output), storage);
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		WorkspaceEntry we = task.getWorkspaceEntry();
		Path<String> path = we.getWorkspacePath();
		String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
		
		Model model = getResolvedStg();
		if (model == null)
		{
			JOptionPane.showMessageDialog(task.getFramework().getMainWindow(), "MPSat output: \n\n" + new String(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getErrors()), "Conflict resolution failed", JOptionPane.WARNING_MESSAGE);
		} else
		{
			final WorkspaceEntry resolved = task.getFramework().getWorkspace().add(path.getParent(), fileName + "_resolved", new ModelEntry(new STGModelDescriptor(), model, storage), true);
			task.getFramework().getMainWindow().createEditorWindow(resolved);
		}
	}
}
