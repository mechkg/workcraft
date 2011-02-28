package org.workcraft.interop;


public interface ServiceProvider {
	<T> T getImplementation(ServiceHandle<T> service) throws ServiceNotAvailableException;
}
