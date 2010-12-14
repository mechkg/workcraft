package org.workcraft.relational.engine;

import java.util.Stack;

import org.workcraft.dependencymanager.advanced.core.Expression;

import pcollections.PMap;

public interface DatabaseEngine {
	void delete(String obj, Id id);
	Id add(String obj, PMap<String, ? extends Object> data);
	void undo();
	Expression<? extends Database> database();
	void setValue(String object, String fieldName, Id id, Object newValue);
}
