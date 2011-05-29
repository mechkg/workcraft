package org.workcraft.interop;

import org.workcraft.util.Function;
import org.workcraft.util.Function0;
import org.workcraft.util.Initialiser;

import pcollections.PMap;

public class ModelServicesImpl extends ServiceProviderImpl<ModelScope, ModelServicesImpl> implements ModelServices {

	static final Function<PMap<ServiceHandle<ModelScope, ?>, Function0<? extends Object>>, ModelServicesImpl> constructor = new Function<PMap<ServiceHandle<ModelScope, ?>, Function0<? extends Object>>, ModelServicesImpl>(){
		@Override
		public ModelServicesImpl apply(PMap<ServiceHandle<ModelScope, ?>, Function0<? extends Object>> argument) {
			return new ModelServicesImpl(argument);
		}
	};
	
	private ModelServicesImpl(PMap<ServiceHandle<ModelScope, ?>, Function0<? extends Object>> map) {
		super(map, constructor);
	}
	
	public static ModelServicesImpl EMPTY = ServiceProviderImpl.empty(constructor);
	
	public <T> T getImplementation(ModelService<T> service) throws ServiceNotAvailableException {
		return super.getImplementation(service.handle);
	}
	
	public <T> ModelServicesImpl plus(ModelService<T> service, T implementation) {
		return this.plus(service.handle, implementation);
	}
	
	public <T> ModelServicesImpl plusDeferred(ModelService<T> service, Initialiser<? extends T> implementation) {
		return this.plusDeferred(service.handle, implementation);
	}
	
}
