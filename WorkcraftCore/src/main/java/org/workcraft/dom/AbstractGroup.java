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

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;

public abstract class AbstractGroup implements Container {
	private Container groupRef;
	
	AbstractGroup (Container groupRef, ModifiableExpression<Node> parent) {
		this.groupRef = groupRef;
		this.parent = parent;
	}
	final ModifiableExpression<Node> parent;
	
	public ModifiableExpression<Node> parent() {
		return parent;
	}

	protected void removeInternal(Node node, boolean notify) {
		if (GlobalCache.eval(node.parent()) != groupRef)
			throw new RuntimeException 
			("Failed to remove a node frome a group because it is not a child of that group ("+node+", parent is " + node.parent() +", expected " + groupRef + ")");

		removeInternal(node);
		GlobalCache.setValue(node.parent(), null);
	}
	
	@Override
	public void add(Node node) {
		if (GlobalCache.eval(node.parent()) != null)
			throw new RuntimeException("Cannot attach someone else's node. Please detach from the old parent first.");
		addInternal(node);
		GlobalCache.setValue(node.parent(), groupRef);
	}

	@Override
	public void add(Collection<Node> nodes) {
		for (Node node : nodes)
			add(node);
	}

	@Override
	public void remove(Node node) {
		removeInternal (node, true);
	}

	@Override
	public void remove(Collection<Node> nodes) {
		LinkedList<Node> nodesToRemove = new LinkedList<Node>(nodes);

		for (Node node : nodesToRemove)
			removeInternal(node, false);
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		for (Node node : nodes)
			removeInternal(node, false);
		newParent.reparent(nodes);
	}
	
	@Override
	public void reparent (Collection<Node> nodes) {
		for (Node node : nodes)
			add(node);
	}

	@Override
	public abstract Expression<? extends Collection<? extends Node>> children();

	protected abstract void addInternal(Node node);
	protected abstract void removeInternal (Node node);
}