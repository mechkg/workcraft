package org.workcraft.plugins.stg;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.plugins.petri.Place;

@VisualClass(org.workcraft.plugins.petri.VisualPlace.class)
public class STGPlace extends Place {
	
	public STGPlace(StorageManager storage) {
		super(storage);
		implicit = storage.create(false);
		capacity = storage.create(1);
	}
	
	private final ModifiableExpression<Boolean> implicit;
	private final ModifiableExpression<Integer> capacity;

	public ModifiableExpression<Integer> capacity() {
		return capacity;
	}

	public ModifiableExpression<Boolean> implicit() {
		return implicit;
	}
}