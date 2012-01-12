package org.workcraft.plugins.mpsat;

import java.io.File;

import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.gates.GateLevelModelDescriptor;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.Workspace;

public class MpsatSynthesisResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> mpsatChainResult;
	private final Workspace workspace;
	private final Path<String> path;

	public MpsatSynthesisResultHandler(Workspace workspace, Path<String> path, Result<? extends MpsatChainResult> mpsatChainResult) {
		this.workspace = workspace;
		this.path = path;
		this.mpsatChainResult = mpsatChainResult;
	}

	@Override
	public void run() {
		final String desiredName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
		final String mpsatOutput = new String(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getOutput());
		new MpsatEqnParser().parse(mpsatOutput);
		//workspace.add(path.getParent(), desiredName, new ModelEntry(new GateLevelModelDescriptor(), , new HistoryPreservingStorageManager()), true);
	}
}
