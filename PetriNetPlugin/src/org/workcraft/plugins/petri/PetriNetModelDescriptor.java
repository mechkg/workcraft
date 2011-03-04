package org.workcraft.plugins.petri;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.interop.ServiceProviderImpl;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;

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

	@Override
	public ServiceProvider createServiceProvider(Model model, StorageManager storage) {
		if (model instanceof PetriNet)
			return getServices((PetriNet)model, (HistoryPreservingStorageManager)storage);
		if (model instanceof VisualPetriNet)
			return getServices((VisualPetriNet)model, (HistoryPreservingStorageManager)storage);
		return ServiceProviderImpl.EMPTY;
	}
	
//	ServiceProviderImpl 

	public static ServiceProviderImpl getServices(VisualPetriNet model, HistoryPreservingStorageManager historyPreservingStorageManager) {
		return getServices((PetriNet)model.getMathModel(), historyPreservingStorageManager)
			.plus(ServiceHandle.LegacyVisualModelService, model);
	}

	public static ServiceProviderImpl getServices(PetriNet model, HistoryPreservingStorageManager historyPreservingStorageManager) {
		return ServiceProviderImpl.EMPTY
		.plus(ModelDescriptor.SERVICE_HANDLE, new PetriNetModelDescriptor())
		.plus(ServiceHandle.LegacyMathModelService, model)
			;
	}
}
