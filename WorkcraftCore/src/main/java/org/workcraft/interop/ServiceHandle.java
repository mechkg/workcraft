package org.workcraft.interop;


/**
 * Instances of this class represent the service type keys for the ServiceProvider interface.
 * @param <T>
 * The type the service has to inherit from.
 * @param <Scope>
 * Identifies the scope this service makes sense in. Serves make unrelated service providers type-incompatible.  
 */
public final class ServiceHandle<Scope, T> {
	
	private Class<T> type;
	private String serviceName;

	public ServiceHandle(Class<T> type, String serviceName) {
		this.type = type;
		this.serviceName = serviceName;
	}

	/**
	 * The reified type T. Required for safe ServiceProvider implementation. 
	 * @return
	 */
	public Class<T> getServiceType() {
		return type;
	}
	
	/**
	 * Creates a new service handle. The services associated with this handle will not be accessible via other handles, even those having the same type. 
	 * @param <T>
	 * The type service implementation must inherit from
	 * @param type
	 * The reified type T
	 * @param serviceName
	 * The name of the service. Used mostly for debugging purposes.
	 * @return
	 * The new service handle with the specified type and serviceName.
	 */
	public static <S,T> ServiceHandle<S,T> createNewService(Class<T> type, String serviceName) {
		return new ServiceHandle<S,T>(type, serviceName);
	}
	
	@Override
	public String toString() {
		return serviceName + " (of type " + type + ")";
	}

	@SuppressWarnings("unchecked")
	public static <S,T> ServiceHandle<S, T> createServiceUnchecked(String serviceName) {
		return new ServiceHandle<S,T>((Class<T>)Object.class, serviceName);
	}
}
