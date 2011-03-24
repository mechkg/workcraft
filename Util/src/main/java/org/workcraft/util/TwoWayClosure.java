package org.workcraft.util;

public interface TwoWayClosure<A,B> {
	public B apply(A a);
	public A reverse(B b);
}
