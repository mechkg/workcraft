package org.workcraft.util;

/**
 * Represents the void value. 
 * Should be used as a type parameter where the values of the parameter type are never needed.
 */
public final class Nothing {
	private Nothing(){}
	public static final Nothing VALUE = new Nothing();
}
