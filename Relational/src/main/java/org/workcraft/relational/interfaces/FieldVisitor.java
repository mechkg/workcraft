package org.workcraft.relational.interfaces;

public interface FieldVisitor<T> {
	T visit(RelationField f);
	T visit(PrimitiveField<?> f);
}