package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.plugins.petri.Place;

@VisualClass("org.workcraft.plugins.petri.VisualPlace")
@DisplayName("Place")
public class STGPlace extends Place {
	private boolean implicit = false;
	private Variable<Integer> capacity = new Variable<Integer>(1);

	public ModifiableExpression<Integer> capacity() {
		return capacity;
	}

	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}

	public boolean isImplicit() {
		return implicit;
	} 
}