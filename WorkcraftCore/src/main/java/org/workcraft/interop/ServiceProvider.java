package org.workcraft.interop;


public interface ServiceProvider<Scope> {
	<T> T getImplementation(ServiceHandle<Scope, T> service) throws ServiceNotAvailableException;
}
