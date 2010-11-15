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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.util.Func;
import org.workcraft.util.Null;


public abstract class StateSupervisor extends HierarchySupervisor {
	
	private final class UpdateExpression extends Expression<Null> {
		@Override
		public Null evaluate(EvaluationContext resolver) {
			for(Expression<?> expr : supervisors.values())
				resolver.resolve(expr);
			return null;
		}

		public void changed() {
			super.refresh();
		}
	}

	private final Func<? super Node, ? extends Expression<?>> supervisionFunc;

	UpdateExpression updateExpression = new UpdateExpression();
	
	public StateSupervisor(Node root, Func<? super Node, ? extends Expression<?>> supervisionFunc) {
		super(root);
		this.supervisionFunc = supervisionFunc;
		GlobalCache.autoRefresh(updateExpression);
	}

	Map<Node, Expression<?>> supervisors = new HashMap<Node, Expression<?>>();
	
	@Override
	public void handleEvent(List<Node> added, List<Node> removed) {
		for(Node n : removed)
			nodeRemoved(n);
		for(Node n : added)
			nodeAdded(n);
		handleHierarchyEvent(added, removed);
		updateExpression.changed();
	}
	
	private void nodeAdded (Node node) {
		Expression<?> supervisor = supervisionFunc.eval(node);
		if(supervisor != null)
			supervisors.put(node, supervisor);
		
		for (Node n : GlobalCache.eval(node.children()))
			nodeAdded(n);
	}
	
	private void nodeRemoved (Node node) {
		supervisors.remove(node);
		
		for (Node n : GlobalCache.eval(node.children()))
			nodeRemoved(n);
	}
	
	protected void handleHierarchyEvent (List<Node> added, List<Node> removed) {
	}
}