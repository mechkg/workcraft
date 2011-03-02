package org.workcraft.util;

/**
 * Function of 0 arguments. Usually depends on side-effects or causes side-effects.
 * @param <R>
 * The type of the function result.
 */
public interface Function0<R> {
    public R apply();
}
