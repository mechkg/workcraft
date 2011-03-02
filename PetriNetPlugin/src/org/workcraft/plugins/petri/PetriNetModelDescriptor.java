package org.workcraft.plugins.petri;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.interop.ServiceProviderImpl;

public class PetriNetModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Petri Net";
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new PetriNet(storage);
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new PetriNetVisualModelDescriptor();
	}

	public ServiceProvider createServiceProvider(PetriNet model) {
		
	}
	
	@Override
	public ServiceProvider createServiceProvider(Model model) {
		if (model instanceof PetriNet)
			return createServiceProvider((PetriNet)model);
		if (model instanceof VisualPetriNet)
			return createServiceProvider((VisualPetriNet)model);
		return ServiceProviderImpl.EMPTY;
	}
}
