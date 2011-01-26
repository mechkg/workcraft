package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;
import org.workcraft.workspace.ModelEntry;

public class PetrifyDummyContractionResult {
	private Result<? extends ExternalProcessResult> petrifyResult;
	private ModelEntry result;
	
	public PetrifyDummyContractionResult(Result<? extends ExternalProcessResult> petrifyResult, ModelEntry result) {
		this.petrifyResult = petrifyResult;
		this.result = result;
	}
	
	public  Result<? extends ExternalProcessResult> getPetrifyResult() {
		return petrifyResult;
	}
	
	public ModelEntry getResult() {
		return result;
	}
}
