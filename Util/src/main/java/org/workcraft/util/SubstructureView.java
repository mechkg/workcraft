package org.workcraft.util;
public interface SubstructureView<A,B> extends Function<A, B> {
	public A reverse(A old, B b);
}
