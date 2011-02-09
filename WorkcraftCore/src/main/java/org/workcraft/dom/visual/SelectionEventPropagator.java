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

package org.workcraft.dom.visual;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Container;
import org.workcraft.dom.HierarchyController;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.SelectionObserver;

import pcollections.PSet;

public class SelectionEventPropagator implements HierarchyController {
	private Expression<? extends Collection<? extends Node>> selection;
	private final HierarchyController next;
	
	public SelectionEventPropagator (ModifiableExpression<PSet<Node>> selection, HierarchyController next) {
		this.next = next;
		this.selection = selection;
	}
	
	@Override
	public void add(Container parent, Node node) {
		adding(node);
		next.add(parent, node);
	}

	private void adding(Node node) {
		if(node instanceof SelectionObserver)
			((SelectionObserver) node).setSelection(selection);
		for(Node n : GlobalCache.eval(node.children()))
			adding(n);
	}

	@Override
	public void remove(Node node) {
		next.remove(node);
	}
}