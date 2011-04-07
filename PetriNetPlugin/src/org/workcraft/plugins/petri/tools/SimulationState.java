package org.workcraft.plugins.petri.tools;

public class SimulationState<ModelState> {
	public final ModelState modelState;
	public final TracePair traces;
	
	public SimulationState(ModelState modelState, TracePair traces) {
		super();
		this.modelState = modelState;
		this.traces = traces;
	}
}
