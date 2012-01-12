package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

import org.workcraft.plugins.stg21.types.MathStg;

import org.workcraft.plugins.stg21.StgModelDescriptor;

public class MpsatCscResolutionResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> mpsatChainResult;
	private final Framework framework;
	private final Path<String> path;

	public MpsatCscResolutionResultHandler(Framework framework, Path<String> path, Result<? extends MpsatChainResult> mpsatChainResult, StorageManager storage) {
				this.framework = framework;
				this.path = path;
				this.mpsatChainResult = mpsatChainResult;
	}
	
	public static MathStg getStg(Result<? extends MpsatChainResult> result)
	{
		final byte[] output = result.getReturnValue().getMpsatResult().getReturnValue().getOutputFile("mpsat.g");
		if(output == null)
			return null;
		
		try {
			return DotGImporter.importStg(new ByteArrayInputStream(output));
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
		
		MathStg mathStg = getStg(mpsatChainResult);
		if (mathStg == null)
		{
			JOptionPane.showMessageDialog(framework.getMainWindow(), "MPSat output: \n\n" + new String(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getErrors()), "Conflict resolution failed", JOptionPane.WARNING_MESSAGE);
		} else
		{
			final WorkspaceEntry resolved = framework.getWorkspace().add(path.getParent(), fileName + "_resolved", StgModelDescriptor.newDocument(mathStg), true);
			try{
			framework.getMainWindow().createEditorWindow(resolved);
			}
			catch(ServiceNotAvailableException e) {
				e.printStackTrace();
				throw new NotImplementedException("ensure with types this does not happen: ");
			}
		}
	}
}
