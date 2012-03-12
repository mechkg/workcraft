package org.workcraft.interop;

public class ServiceNotAvailableException extends Exception {

	private static final long serialVersionUID = -3627890206434160794L;
	
	public ServiceNotAvailableException(Throwable cause) {
		super(cause);
	}
	
	public ServiceNotAvailableException(String message) {
		super(message);
	}
	
	public ServiceNotAvailableException(ServiceHandle<?, ?> service) {
		super("Service not available: " + service.toString());
	}
	
	

}
