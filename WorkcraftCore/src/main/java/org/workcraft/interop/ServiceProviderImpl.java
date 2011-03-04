package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.util.Function0;
import org.workcraft.util.Initialiser;

import pcollections.HashTreePMap;
import pcollections.PMap;

import static org.workcraft.interop.LazyObjectProvider.*;

public class ServiceProviderImpl implements ServiceProvider {

	ServiceProviderImpl(PMap<ServiceHandle<?>, Function0<? extends Object>> map) {
		this.map = map;
	}
	
	public static ServiceProvider createLegacyServiceProvider(Model model) {
		throw new NotSupportedException("Maybe we should not even start implementing this? It is already deprecated anyway.");
	}
	
	
	//haskell: constant value = return value
	private <T> Function0<T> constant(final T value) {
		return new Function0<T>(){
			@Override
			public T apply() {
				return value;
			}
		};
	}
	
	public <T> ServiceProviderImpl plusAll(ServiceProviderImpl p) {
		return new ServiceProviderImpl(map.plusAll(p.map));
	}
	
	public <T> ServiceProviderImpl plus(ServiceHandle<T> service, T implementation) {
		return new ServiceProviderImpl(map.plus(service, constant(implementation)));
	}
	
	public <T> ServiceProviderImpl plusDeferred(ServiceHandle<T> service, Initialiser<? extends T> implementation) {
		return new ServiceProviderImpl(map.plus(service, lazy(implementation)));
	}
	
	public static ServiceProviderImpl EMPTY = new ServiceProviderImpl(HashTreePMap.<ServiceHandle<?>, Function0<? extends Object>>empty());
	
	private final PMap<ServiceHandle<?>, Function0<? extends Object>> map;
	
	@Override
	public <T> T getImplementation(ServiceHandle<T> service) throws ServiceNotAvailableException {
		Function0<? extends Object> res = map.get(service);
		if (res == null)
			throw new ServiceNotAvailableException(service);
		return service.getServiceType().cast(res.apply());
	}
}
