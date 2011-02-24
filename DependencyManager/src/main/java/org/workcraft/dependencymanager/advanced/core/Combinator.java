package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.util.Function;

public interface Combinator<T1, T2> extends Function<T1, Expression<? extends T2>> {
}
