package org.workcraft.relational.engine;

import java.util.Collection;

import org.workcraft.relational.interfaces.Relation;

import pcollections.HashTreePMap;
import pcollections.PMap;

public class DatabaseImpl implements Database {

	public DatabaseImpl(Collection<? extends Relation> schema) {
		PMap<String, PMap<Id, PMap<String, ? extends Object>>> data = HashTreePMap.empty();
		for(Relation r : schema)
			data = data.plus(r.getObjectDeclaration().name(), HashTreePMap.<Id, PMap<String, ? extends Object>>empty());
		this.data = data;
	}
	
	public DatabaseImpl(PMap<String, PMap<Id, PMap<String, ? extends Object>>> newData) {
		data = newData;
	}

	public final PMap<String, PMap<Id, PMap<String, ? extends Object>>> data;
	
	@Override
	public Object get(String obj, String field, Id id) {
		return data.get(obj).get(id).get(field);
	}

	@Override
	public Collection<Id> list(String obj) {
		return data.get(obj).keySet();
	}

}
