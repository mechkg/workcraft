package org.workcraft.plugins.petrify.tasks;

import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class PetrifyDummyContractionResult {
	private Result<? extends ExternalProcessResult> petrifyResult;
	private ServiceProvider result;
	
	public PetrifyDummyContractionResult(Result<? extends ExternalProcessResult> petrifyResult, ServiceProvider result) {
		this.petrifyResult = petrifyResult;
		this.result = result;
	}
	
	public  Result<? extends ExternalProcessResult> getPetrifyResult() {
		return petrifyResult;
	}
	
	public ServiceProvider getResult() {
		return result;
	}
}
