package org.workcraft.relational.engine;

import java.util.Collection;

import org.workcraft.relational.engine.Id;

public interface Database {
	public Object get(String obj, String field, Id id);
	public Collection<Id> list(String obj);
}
