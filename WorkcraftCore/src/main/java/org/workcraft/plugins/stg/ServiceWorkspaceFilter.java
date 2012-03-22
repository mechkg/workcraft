package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceFilter;
import org.workcraft.interop.ModelService;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.workspace.WorkspaceEntry;

public class ServiceWorkspaceFilter<T> implements WorkspaceFilter<T> {
	private final Framework framework;
	private final ModelService<T> serviceHandle;
	
	public ServiceWorkspaceFilter(Framework framework, ModelService<T> serviceHandle) {
		this.framework = framework;
		this.serviceHandle = serviceHandle;
	}
	
	@Override
	public T interpret(Path<String> arg) {
		WorkspaceEntry entry = framework.getWorkspace().getOpenFile(arg);
		try { return entry.getModelEntry().getImplementation(serviceHandle); }
		catch(ServiceNotAvailableException e) {return null;} 
	}
}
