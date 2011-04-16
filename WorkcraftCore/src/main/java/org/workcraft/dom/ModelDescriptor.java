package org.workcraft.dom;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.interop.ServiceProvider;

public interface ModelDescriptor {
	ServiceHandle<ModelDescriptor> SERVICE_HANDLE = ServiceHandle.createNewService(ModelDescriptor.class, "Model descriptor");
	String getDisplayName();
	/**
	 * This method returns a ServiceProvider object representing all the services the model is capable of providing.
	 */
	ServiceProvider newDocument();
	/**
	 * The method is called by the 'load' mechanism. This should be eliminated somehow.
	 * @param model
	 * @param storage
	 * @return
	 */
	@Deprecated
	ServiceProvider createServiceProvider(Model model, StorageManager storage);
}
