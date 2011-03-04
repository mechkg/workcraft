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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.HierarchyObservingState;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.util.Hierarchy;

public class DefaultMathNodeRemover implements HierarchyObserver {
	
	static class ReferenceCounter implements HierarchyObservingState<List<MathNode>>
	{
		private HashMap<MathNode, Integer> refCount = new HashMap<MathNode, Integer>();
		public void incRef (MathNode node) {
			if (refCount.get(node) == null)
				refCount.put(node, 1);
			else
				refCount.put(node, refCount.get(node)+1);
		}

		public void decRef (MathNode node) {
			Integer refs = refCount.get(node)-1;
			if (refs == 0) {
				// System.out.println ( "Math node " + node + " is no longer referenced to, deleting");
				toRemove.add(node);
				refCount.remove(node);
			} else
				refCount.put(node, refs);
		}
		
		private void nodeAdded(Node node) {
			//System.out.println("math node remover: node added: " + node);
			if (node instanceof DependentNode)
				for (MathNode mn : ((DependentNode)node).getMathReferences())
					incRef(mn);
			
			for (Node n : GlobalCache.eval(node.children()))
				nodeAdded(n);
		}

		private void nodeRemoved(Node node) {
			//System.out.println("math node remover: node removed: " + node);
			if (node instanceof DependentNode)
				for (MathNode mn : ((DependentNode)node).getMathReferences())
					decRef(mn);
			
			for (Node n : GlobalCache.eval(node.children()))
				nodeRemoved(n);
		}

		@Override
		public void handleEvent(Collection<? extends Node> added,Collection<? extends Node> removed) {
			for(Node node : added)
				nodeAdded(node);
			for(Node node : removed)
				nodeRemoved(node);
		}
		
		List<MathNode> toRemove = new ArrayList<MathNode>();

		@Override
		public List<MathNode> getState() {
			return toRemove;
		}
	}
	
	private final Model model;
	final ReferenceCounter refCounter;
	final Expression<List<MathNode>> staleNodes;
	
	public DefaultMathNodeRemover(Node root, Model mathModel) {
		this.refCounter = new ReferenceCounter();
		this.staleNodes = new HierarchySupervisor<List<MathNode>>(root, refCounter);
		this.model = mathModel;
	}

	@Override
	public void handleEvent(Collection<? extends Node> added, Collection<? extends Node> removed) {
		List<MathNode> stale = GlobalCache.eval(staleNodes);
		for(MathNode node : stale)
			if(Hierarchy.isDescendant(node, model.getRoot()))
				model.remove(node);
		stale.clear(); // clears the internal list of the ReferenceCounter
	}

}