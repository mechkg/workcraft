package org.workcraft.relational.interfaces;

public interface Field {
	<T> T accept(FieldVisitor<T> visitor);
}