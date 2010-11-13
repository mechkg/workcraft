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

import java.util.HashMap;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.observation.HierarchySupervisor;

public class DefaultMathNodeRemover extends HierarchySupervisor {
	public DefaultMathNodeRemover(Node root) {
		super(root);
	}

	private HashMap<MathNode, Integer> refCount = new HashMap<MathNode, Integer>();
	private void incRef (MathNode node) {
		if (refCount.get(node) == null)
			refCount.put(node, 1);
		else
			refCount.put(node, refCount.get(node)+1);
	}

	private void decRef (MathNode node) {
		Integer refs = refCount.get(node)-1;
		if (refs == 0) {
			// System.out.println ( "Math node " + node + " is no longer referenced to, deleting");
			refCount.remove(node);
			if (GlobalCache.eval(node.parent()) instanceof Container)
				((Container)GlobalCache.eval(node.parent())).remove(node);		
		} else
			refCount.put(node, refs);
	}

	@Override
	public void handleEvent(List<Node> added, List<Node> removed) {
		for (Node node : removed)
			nodeRemoved(node);
	
		for (Node node : added)
			nodeAdded(node);
	}

	private void nodeAdded(Node node) {
		if (node instanceof DependentNode)
			for (MathNode mn : ((DependentNode)node).getMathReferences())
				incRef(mn);
		
		for (Node n : GlobalCache.eval(node.children()))
			nodeAdded(n);
	}

	private void nodeRemoved(Node node) {
		if (node instanceof DependentNode)
			for (MathNode mn : ((DependentNode)node).getMathReferences())
				decRef(mn);
		
		for (Node n : GlobalCache.eval(node.children()))
			nodeRemoved(n);
	}

}