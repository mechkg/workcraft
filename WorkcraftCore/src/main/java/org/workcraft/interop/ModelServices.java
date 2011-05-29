package org.workcraft.interop;


public interface ModelServices {
	<T> T getImplementation(ModelService<T> service) throws ServiceNotAvailableException;
}
