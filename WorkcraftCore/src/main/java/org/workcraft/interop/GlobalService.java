package org.workcraft.interop;

public final class GlobalService<T> {
	final ServiceHandle<GlobalScope, T> handle;

	public GlobalService(ServiceHandle<GlobalScope, T> handle) {
		this.handle = handle;
	}

	public static <T> GlobalService<T> createNewService(Class<T> type, String serviceName) {
		ServiceHandle<GlobalScope, T> newService = ServiceHandle.createNewService(type, serviceName);
		return new GlobalService<T>(newService);
	} 
}
