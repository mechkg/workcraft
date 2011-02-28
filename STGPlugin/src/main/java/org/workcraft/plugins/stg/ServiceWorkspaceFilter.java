package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceFilter;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.workspace.WorkspaceEntry;

import checkers.nullness.quals.Nullable;

public class ServiceWorkspaceFilter<T> implements WorkspaceFilter<T> {
	private final Framework framework;
	private final ServiceHandle<T> serviceHandle;
	
	public ServiceWorkspaceFilter(Framework framework, ServiceHandle<T> serviceHandle) {
		this.framework = framework;
		this.serviceHandle = serviceHandle;
	}
	
	@Override
	public @Nullable T interpret(Path<String> arg) {
		WorkspaceEntry entry = framework.getWorkspace().getOpenFile(arg);
		try { return entry.getModelEntry().services.getImplementation(serviceHandle); }
		catch(ServiceNotAvailableException e) {return null;} 
	}
}
