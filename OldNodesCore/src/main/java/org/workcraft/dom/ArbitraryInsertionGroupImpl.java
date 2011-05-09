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

import java.util.LinkedList;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.connections.VisualConnection;

public class ArbitraryInsertionGroupImpl extends AbstractGroup implements Container {
	
	final ModifiableExpression<LinkedList<Node>> children;
	
	public ArbitraryInsertionGroupImpl (Container groupRef, VisualConnection parent, StorageManager storage) {
		super(groupRef, storage.<Node>create(parent));
		children = storage.create(new LinkedList<Node>());
	}

	public void add(int index, Node node) {
		LinkedList<Node> newValue = new LinkedList<Node>(eval(children));
		newValue.add(index, node); // TODO: use PCollections?
		children.setValue(newValue);
	}

	@Override
	protected void addInternal(Node node) {
		LinkedList<Node> newValue = new LinkedList<Node>(eval(children));
		newValue.add(node);
		children.setValue(newValue);
	}

	@Override
	protected void removeInternal(Node node) {
		LinkedList<Node> newValue = new LinkedList<Node>(eval(children));
		newValue.remove(node);
		children.setValue(newValue);
	}

	@Override
	public Expression<? extends LinkedList<Node>> children() {
		return children;
	}
}
