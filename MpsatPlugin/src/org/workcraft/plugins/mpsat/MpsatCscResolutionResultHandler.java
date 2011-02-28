package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCscResolutionResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> mpsatChainResult;
	private final StorageManager storage;
	private final Framework framework;
	private final Path<String> path;

	public MpsatCscResolutionResultHandler(Framework framework, Path<String> path, Result<? extends MpsatChainResult> mpsatChainResult, StorageManager storage) {
				this.framework = framework;
				this.path = path;
				this.mpsatChainResult = mpsatChainResult;
				this.storage = storage;
	}
	
	public static STGModel getStg(Result<? extends MpsatChainResult> result, StorageManager storage)
	{
		final byte[] output = result.getReturnValue().getMpsatResult().getReturnValue().getOutputFile("mpsat.g");
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
		String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
		
		Model model = getStg(mpsatChainResult, storage);
		if (model == null)
		{
			JOptionPane.showMessageDialog(framework.getMainWindow(), "MPSat output: \n\n" + new String(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getErrors()), "Conflict resolution failed", JOptionPane.WARNING_MESSAGE);
		} else
		{
			final WorkspaceEntry resolved = framework.getWorkspace().add(path.getParent(), fileName + "_resolved", new ModelEntry(new STGModelDescriptor(), model, storage), true);
			framework.getMainWindow().createEditorWindow(resolved);
		}
	}
}
