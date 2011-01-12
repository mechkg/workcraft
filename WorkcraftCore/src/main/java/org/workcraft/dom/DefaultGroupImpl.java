/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;

public class DefaultGroupImpl extends AbstractGroup implements Container {
	ModifiableExpression<LinkedHashSet<Node>> children;
	
	public DefaultGroupImpl (Container groupRef, StorageManager storage) {
		super(groupRef, storage.<Node>create(null));
		children = storage.create(new LinkedHashSet<Node>());
	}

	@Override
	protected void addInternal(Node node) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(eval(children));
		newValue.add(node);
		children.setValue(newValue);
	}

	@Override
	protected void removeInternal(Node node) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(eval(children));
		if(newValue.remove(node))
			children.setValue(newValue);
	}

	@Override
	public Expression<? extends Collection<Node>> children() {
		return children;
	}

}
