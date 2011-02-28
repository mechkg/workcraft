package org.workcraft.dom;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.ServiceProvider;


public interface ModelDescriptor {
	String getDisplayName();
	MathModel createMathModel(StorageManager storage);
	VisualModelDescriptor getVisualModelDescriptor();
	/**
	 * This method returns a ServiceProvider object representing all the services the model is capable of providing.
	 * @param model
	 * The model for which to provide the services. Hopefully, this parameter will be soon eliminated along with all other methods of the ModelDescriptor interface.
	 * @return
	 */
	ServiceProvider createServiceProvider(Model model); 
}
