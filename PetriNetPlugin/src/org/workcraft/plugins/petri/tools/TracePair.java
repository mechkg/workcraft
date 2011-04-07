package org.workcraft.plugins.petri.tools;

import org.workcraft.Trace;

public class TracePair {
	public final Trace trace;
	public final Trace branchTrace;
	public int traceStep;
	public int branchStep;

	public TracePair(Trace trace, int traceStep, Trace branchTrace, int branchStep) {
		this.trace = trace;
		this.branchTrace = branchTrace;
		this.traceStep = traceStep;
		this.branchStep = branchStep;
	}
	
	public static TracePair createEmpty(){
		return new TracePair(new Trace(), 0, new Trace(), 0);
	}

	public TracePair clone() {
		return new TracePair(trace.clone(), traceStep, branchTrace.clone(), branchStep);
	}
}
