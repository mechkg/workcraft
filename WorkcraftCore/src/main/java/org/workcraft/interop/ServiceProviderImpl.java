package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.util.Function;
import org.workcraft.util.Function0;
import org.workcraft.util.Initialiser;

import pcollections.HashTreePMap;
import pcollections.PMap;

import static org.workcraft.interop.LazyObjectProvider.*;

public class ServiceProviderImpl<S, M extends ServiceProviderImpl<S, M>> implements ServiceProvider<S> {

	private final Function<PMap<ServiceHandle<S, ?>, Function0<? extends Object>>, M> copyConstructor;

	ServiceProviderImpl(PMap<ServiceHandle<S, ?>, Function0<? extends Object>> map, Function<PMap<ServiceHandle<S, ?>, Function0<? extends Object>>, M> copyConstructor) {
		this.map = map;
		this.copyConstructor = copyConstructor;
	}
	
	public static <S> ServiceProvider<S> createLegacyServiceProvider(Model model) {
		throw new NotSupportedException("Maybe we should not even start implementing this? It is already deprecated anyway.");
	}
	
	
	private <T> Function0<T> constant(final T value) {
		return new Function0<T>(){
			@Override
			public T apply() {
				return value;
			}
		};
	}
	
	public M plusAll(M p) {
		return copyConstructor.apply(map.plusAll(p.map));
	}
	
	public <T> M plus(ServiceHandle<S, T> service, T implementation) {
		return copyConstructor.apply(map.plus(service, constant(implementation)));
	}
	
	public <T> M plusDeferred(ServiceHandle<S, T> service, Initialiser<? extends T> implementation) {
		return copyConstructor.apply(map.plus(service, lazy(implementation)));
	}
	
	public static <S, M extends ServiceProviderImpl<S, M>> M empty(Function<PMap<ServiceHandle<S, ?>, Function0<? extends Object>>, M> copyConstructor) { 
		return copyConstructor.apply(HashTreePMap.<ServiceHandle<S, ?>, Function0<? extends Object>>empty());
	}
	
	private final PMap<ServiceHandle<S, ?>, Function0<? extends Object>> map;
	
	@Override
	public <T> T getImplementation(ServiceHandle<S, T> service) throws ServiceNotAvailableException {
		Function0<? extends Object> res = map.get(service);
		if (res == null)
			throw new ServiceNotAvailableException(service);
		return service.getServiceType().cast(res.apply());
	}
}
