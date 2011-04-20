package org.workcraft.util;
public interface FieldAccessor<A,B> extends Function<A, B> {
	public A assign(A object, B value);
}
