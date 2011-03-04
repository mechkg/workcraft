package org.workcraft.util;

/**
 * Function of 0 arguments. Usually depends on side-effects or causes side-effects.
 * @param <R>
 * The type of the function result.
 */
public interface Function0<R> {
    class Util {
    	public static <R> Function0<R> constant(final R value) {
    		return new Function0<R>(){
    			@Override
    			public R apply() {
        			return value;
    			}
    		};
    	}
    }

	public R apply();
}
