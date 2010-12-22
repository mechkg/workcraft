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
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression;
import org.workcraft.dom.Node;
import org.workcraft.util.Null;

public abstract class HierarchySupervisor {
	
	public HierarchySupervisor(Node root) {
		if(root == null)
			throw new NullPointerException();
		rootSupervisor = new SupervisingNode(root);
	}
	
	public void start() {
		registerChange(Arrays.asList(new Node[]{rootSupervisor.node}), Arrays.asList(new Node[]{}));
		autoRefresh = GlobalCache.autoRefresh(rootSupervisor);
	}
	
	public void refresh() {
		GlobalCache.eval(rootSupervisor);
	}
	
	@SuppressWarnings("unused") // need so that autoRefresh does not get garbage collected
	private AutoRefreshExpression autoRefresh;
	private SupervisingNode rootSupervisor;
	
	class SupervisingNode extends ExpressionBase<Null> {
		Map<Node, SupervisingNode> latest = new HashMap<Node, SupervisingNode>();
		final Node node;

		public SupervisingNode(Node node) {
			if(node == null)
				throw new NullPointerException("node");
			this.node = node;
		}
		
		@Override
		public Null evaluate(EvaluationContext resolver) {
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
			
			return Null.Null;
		}
	}
	
	private void registerChange(List<Node> added, List<Node> removed) {
		handleEvent(added, removed);
	}
	
	public abstract void handleEvent (List<Node> added, List<Node> removed);
}
