package org.workcraft.interop;

import org.workcraft.util.Function0;
import org.workcraft.util.Initialiser;

public class LazyObjectProvider<T> implements Function0<T> {

	private final Initialiser<T> initialiser;
	private T obj = null;

	public LazyObjectProvider(Initialiser<T> initialiser) {
		this.initialiser = initialiser;
	}
	
	@Override
	public T apply() {
		if(obj == null)
			obj = initialiser.create();
		return obj;
	}

	public static <T> Function0<T> lazy(Initialiser<T> initialiser) {
		return new LazyObjectProvider<T>(initialiser);
	}
	
	public static <T> Initialiser<T> asInitialiser(final Function0<T> func) {
		return new Initialiser<T>() {

			@Override
			public T create() {
				return func.apply();
			}
		};
	}
}
