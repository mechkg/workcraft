package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Trace;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.mpsat.gui.SolutionsDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;


final class MpsatStgReachabilityResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> mpsatChainResult;
	private final MainWindow mainWindow;
	private final WorkspaceEntry we;
	
	MpsatStgReachabilityResultHandler(
			MainWindow mainWindow,
			WorkspaceEntry we,
			Result<? extends MpsatChainResult> mpsatChainResult) {
		this.mainWindow = mainWindow;
		this.we = we;
		this.mpsatChainResult = mpsatChainResult;
	}
	
	
	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue());

		List<Trace> solutions = mdp.getSolutions();
		
		if (!solutions.isEmpty()) {
			String message = "The system has a non-persistent output.\n";
			
			SolutionsDialog solutionsDialog;
			try {
				solutionsDialog = new SolutionsDialog(mainWindow, we, message, solutions);
			} catch (ServiceNotAvailableException e) {
				throw new NotImplementedException("Should never happen. TODO: make the type system take care of this");
			}
			
			GUI.centerAndSizeToParent(solutionsDialog, mainWindow);
			
			solutionsDialog.setVisible(true);
		} else
			JOptionPane.showMessageDialog(null, "All system outputs are persistent.");
	}

}
