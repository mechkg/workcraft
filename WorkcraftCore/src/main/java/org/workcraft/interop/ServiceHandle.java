package org.workcraft.interop;


/**
 * Instances of this class represent the service type keys for the ServiceProvider interface.
 * @param <T>
 * The type the service has to inherit from.
 */
public final class ServiceHandle<T> {
	
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
	
	public static <T> ServiceHandle<T> createNewService(Class<T> type, String serviceName) {
		return new ServiceHandle<T>(type, serviceName);
	}
	
	@Override
	public String toString() {
		return serviceName;
	}
}
