package org.workcraft.interop;

import java.util.HashMap;
import java.util.Map;

public class ServiceProviderImpl implements ServiceProvider {

	public ServiceProviderImpl() {
	}
	
	public <T> void addImplementation(ServiceHandle<T> service, T implementation) {
		map.put(service, implementation);
	}
	
	Map<ServiceHandle<?>, Object> map = new HashMap<ServiceHandle<?>, Object>();
	
	@Override
	public <T> T getImplementation(ServiceHandle<T> service) throws ServiceNotAvailableException {
		Object res = map.get(service);
		if (res == null)
			throw new ServiceNotAvailableException(service);
		return service.getServiceType().cast(res);
	}
}