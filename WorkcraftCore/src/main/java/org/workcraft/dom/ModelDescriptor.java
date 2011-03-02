package org.workcraft.dom;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;


public interface ModelDescriptor {
	ServiceHandle<ModelDescriptor> SERVICE_HANDLE = ServiceHandle.createNewService(ModelDescriptor.class, "Model descriptor");
	String getDisplayName();
	MathModel createMathModel(StorageManager storage);
	VisualModelDescriptor getVisualModelDescriptor();
	/**
	 * This method returns a ServiceProvider object representing all the services the model is capable of providing.
	 * @param model
	 * The model for which to provide the services. Hopefully, this parameter will be soon eliminated along with all other methods of the ModelDescriptor interface.
	 * @param storage 
	 * @return
	 */
	ServiceProvider createServiceProvider(Model model, StorageManager storage); 
}
