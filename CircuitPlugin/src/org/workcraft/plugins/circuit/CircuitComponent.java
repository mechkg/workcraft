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

package org.workcraft.plugins.circuit;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.util.Hierarchy;

@DisplayName("Component")
@VisualClass(org.workcraft.plugins.circuit.VisualCircuitComponent.class)

public class CircuitComponent extends MathNode implements Container {
	
	public CircuitComponent(StorageManager storage) {
		super(storage);
		groupImpl = new DefaultGroupImpl(this, storage);
		name = storage.create("");
		isEnvironment = storage.create(false);
	}

	final DefaultGroupImpl groupImpl;
	private final ModifiableExpression<String> name;
	private final ModifiableExpression<Boolean> isEnvironment;

	public ModifiableExpression<Boolean> isEnvironment() {
		return isEnvironment;
	}

	public ModifiableExpression<Node> parent() {
		return groupImpl.parent();
	}

	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	@Override
	public void remove(Node node) {
		groupImpl.remove(node);
	}

	@Override
	public void remove(Collection<Node> node) {
		groupImpl.remove(node);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
		checkName(newParent);
	}

	@Override
	public Expression<? extends Collection<? extends Node>> children() {
		return groupImpl.children();
	}

	public Collection<Contact> getContacts() {
		return Hierarchy.filterNodesByType(eval(children()), Contact.class);
	}
	
	public Collection<Contact> getInputs() {
		ArrayList<Contact> result = new ArrayList<Contact>(); 
		for(Contact c : getContacts())
			if(eval(c.ioType()) == IoType.INPUT)
				result.add(c);
		return result;
	}

	public String getNewName(Node n, String start) {
		// iterate through all contacts, check that the name doesn't exist
		int num=0;
		boolean found = true;
		
		while (found) {
			num++;
			found=false;
			
			for (Node vn : eval(n.children())) {
				if (vn instanceof CircuitComponent && vn!=this) {
					if (eval(((CircuitComponent)vn).name()).equals(start+num)) {
						found=true;
						break;
					}
				}
			}
		}
		return start+num;
	}
	
	public void checkName(Node parent) {
		if (parent==null) return;
		String start=eval(name());
		if (start==null||start=="") {
			start="c";
			name().setValue(getNewName(parent, start));
		}
	}

	public ModifiableExpression<String> name() {
		return name;
	}
}
