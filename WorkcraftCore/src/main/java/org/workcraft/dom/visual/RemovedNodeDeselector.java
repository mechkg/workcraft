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

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;

import pcollections.PSet;

public class RemovedNodeDeselector extends ExpressionBase<PSet<Node>> {
	private final Expression<PSet<Node>> selection;
	private final Node root;
	
	public RemovedNodeDeselector(Node root, Expression<PSet<Node>> selection) {
		this.root = root;
		this.selection = selection;
	}

	@Override
	protected PSet<Node> evaluate(EvaluationContext context) {
		PSet<Node> result = context.resolve(selection);
		for(Node node : result) {
			if(isRemoved(node, context))
				result = result.minus(node);
		}
		return result;
	}

	private boolean isRemoved(Node node, EvaluationContext context) {
		if(node==null)
			return true;
		if(node == root)
			return false;
		return isRemoved(context.resolve(node.parent()), context);
	}
}