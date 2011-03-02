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

package org.workcraft.observation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;
import org.workcraft.util.Nothing;

import pcollections.HashTreePMap;

public class HierarchySupervisor<STATE> extends ExpressionBase<STATE> {
	
	private final HierarchyObservingState<? extends STATE> observer;

	public HierarchySupervisor(Node root, HierarchyObservingState<? extends STATE> observer) {
		this.observer = observer;
		if(root == null)
			throw new NullPointerException();
		rootSupervisor = new SupervisingNode(root);
		registerChange(Arrays.asList(new Node[]{rootSupervisor.node}), Arrays.asList(new Node[]{}));
	}
	
	private SupervisingNode rootSupervisor;
	
	class SupervisingNode extends ExpressionBase<Nothing> {
		Map<Node, SupervisingNode> latest = HashTreePMap.empty();
		final Node node;

		public SupervisingNode(Node node) {
			if(node == null)
				throw new NullPointerException("node");
			this.node = node;
		}
		
		@Override
		public Nothing evaluate(EvaluationContext resolver) {
			Collection<? extends Node> newChildren = resolver.resolve(node.children());
			final List<Node> added = new ArrayList<Node>(newChildren);
			added.removeAll(latest.keySet());
			final List<Node> removed = new ArrayList<Node>(latest.keySet());
			removed.removeAll(newChildren);
			
			Map<Node, SupervisingNode> newMap = new HashMap<Node, SupervisingNode>();
			for(Node n : newChildren) {
				SupervisingNode supervisor = latest.get(n);
				if(supervisor==null)
					supervisor = new SupervisingNode(n);
				newMap.put(n, supervisor);
				resolver.resolve(supervisor);
			}
			registerChange(added, removed);
			
			latest = newMap;
			
			return Nothing.VALUE;
		}
	}
	
	private void registerChange(List<Node> added, List<Node> removed) {
		observer.handleEvent(added, removed);
	}
	
	@Override
	protected STATE evaluate(EvaluationContext context) {
		context.resolve(rootSupervisor);
		return observer.getState();
	}
}
