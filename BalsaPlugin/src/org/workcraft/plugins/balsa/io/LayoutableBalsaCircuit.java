package org.workcraft.plugins.balsa.io;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.interop.ModelService;

public interface LayoutableBalsaCircuit {
	public ModelService<LayoutableBalsaCircuit> SERVICE_HANDLE = ModelService.createNewService(LayoutableBalsaCircuit.class, "A Balsa circuit supporting automatic visual node layout");
	
	// TODO: something more specific!
	public Model getModel();
	public TouchableProvider<Node> getTouchableProvider();
}
