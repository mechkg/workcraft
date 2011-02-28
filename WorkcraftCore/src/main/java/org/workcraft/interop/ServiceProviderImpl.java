package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.NotSupportedException;

import pcollections.HashTreePMap;
import pcollections.PMap;

public class ServiceProviderImpl implements ServiceProvider {

	ServiceProviderImpl(PMap<ServiceHandle<?>, Object> map) {
		this.map = map;
	}
	
	public static ServiceProvider createLegacyServiceProvider(Model model) {
		throw new NotSupportedException("Maybe we should not even start implementing this? It is already deprecated anyway.");
	}
	
	public <T> ServiceProviderImpl plusImplementation(ServiceHandle<T> service, T implementation) {
		return new ServiceProviderImpl(map.plus(service, implementation));
	}
	
	public static ServiceProviderImpl EMPTY = new ServiceProviderImpl(HashTreePMap.<ServiceHandle<?>, Object>empty());
	
	private final PMap<ServiceHandle<?>, Object> map;
	
	@Override
	public <T> T getImplementation(ServiceHandle<T> service) throws ServiceNotAvailableException {
		Object res = map.get(service);
		if (res == null)
			throw new ServiceNotAvailableException(service);
		return service.getServiceType().cast(res);
	}
}