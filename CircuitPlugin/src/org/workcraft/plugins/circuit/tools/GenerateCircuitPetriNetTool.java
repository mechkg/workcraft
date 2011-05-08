package org.workcraft.plugins.circuit.tools;

import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitPetriNetGenerator;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class GenerateCircuitPetriNetTool implements Tool {

	private final Workspace ws;

	public GenerateCircuitPetriNetTool(Workspace ws)
	{
		this.ws = ws;
	}
	
	@Override
	public String getDisplayName() {
		return "Generate STG";
	}

	@Override
	public String getSection() {
		return "STG";
	}

	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {

		final VisualCircuit circuit = we.getModelEntry().getImplementation(VisualCircuit.SERVICE_HANDLE);
		
		return new ToolJob(){
			@Override
			public void run() {
				HistoryPreservingStorageManager storage = new HistoryPreservingStorageManager();
				VisualSTG vstg = CircuitPetriNetGenerator.generate(circuit, storage);
				ws.add(we.getWorkspacePath().getParent(), we.getWorkspacePath().getNode(), 
						STGModelDescriptor.getServices(vstg, storage), false);
			}
		};
	}
}
