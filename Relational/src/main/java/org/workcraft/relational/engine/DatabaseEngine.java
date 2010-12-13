package org.workcraft.relational.engine;

import org.workcraft.dependencymanager.advanced.core.Expression;

import pcollections.PMap;

public interface DatabaseEngine {
	void delete(String obj, Id id);
	Id add(String obj, PMap<String, ? extends Object> data);
	Expression<? extends Database> database();
}
