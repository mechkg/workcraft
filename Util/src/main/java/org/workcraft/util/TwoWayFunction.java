package org.workcraft.util;

public interface TwoWayFunction<A,B> extends Function<A,B> {
	public A reverse(B b);
}
