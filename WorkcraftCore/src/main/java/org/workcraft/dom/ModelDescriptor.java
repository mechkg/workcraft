package org.workcraft.dom;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.interop.GlobalService;
import org.workcraft.interop.ModelService;
import org.workcraft.interop.ModelServices;

public interface ModelDescriptor {
	GlobalService<ModelDescriptor> GLOBAL_SERVICE_HANDLE = GlobalService.createNewService(ModelDescriptor.class, "Model descriptor");
	ModelService<ModelDescriptor> SERVICE_HANDLE = ModelService.createNewService(ModelDescriptor.class, "Model descriptor");
	String getDisplayName();
	/**
	 * This method returns a ServiceProvider object representing all the services the model is capable of providing.
	 */
	ModelServices newDocument();
	/**
	 * The method is called by the 'load' mechanism. This should be eliminated somehow.
	 * @param model
	 * @param storage
	 * @return
	 */
	@Deprecated
	ModelServices createServiceProvider(Model model, StorageManager storage);
}
