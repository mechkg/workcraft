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

package org.workcraft.dom.math;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;

public class MathGroup extends MathNode implements Container {
	final DefaultGroupImpl groupImpl;

	public MathGroup(StorageManager storage) {
		super(storage);
		groupImpl = new DefaultGroupImpl(this, storage);
	}
	
	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public ModifiableExpression<Node> parent() {
		return groupImpl.parent();
	}

	@Override
	public void remove(Node node) {
		groupImpl.remove(node);
	}

	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	@Override
	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}
	
	@Override
	public Expression<? extends Collection<Node>> children() {
		return groupImpl.children();
	}
}