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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.HierarchyObservingState;

public class NodeContextTracker implements NodeContext, HierarchyObserver, HierarchyObservingState<NodeContext> {
	public NodeContextTracker() {
	}

	HashMap<Node, LinkedHashSet<Node>> presets = new HashMap<Node, LinkedHashSet<Node>>();
	HashMap<Node, LinkedHashSet<Node>> postsets = new HashMap<Node, LinkedHashSet<Node>>();
	HashMap<Node, LinkedHashSet<Connection>> connections = new HashMap<Node, LinkedHashSet<Connection>>();

	private void initHashes (Node n) {
		if (presets.get(n) == null)
			presets.put(n, new LinkedHashSet<Node>());
		if (postsets.get(n) == null)
			postsets.put(n, new LinkedHashSet<Node>());
		if (connections.get(n) == null)
			connections.put(n, new LinkedHashSet<Connection>());
	}

	private void removeHashes (Node n) {
		presets.remove(n);
		postsets.remove(n);
		connections.remove(n);
	}

	private void nodeAdded (Node n) {
		//System.out.println ("(NCT) node added " + n);
		initHashes(n);
		
		if (n instanceof Connection) {
			Connection con = (Connection)n;
			Node c1 = con.getFirst();
			Node c2 = con.getSecond();

			initHashes(c1);
			initHashes(c2);

			postsets.get(c1).add(c2);
			presets.get(c2).add(c1);
			connections.get(c1).add(con);
			connections.get(c2).add(con);
		}
		
		for (Node nn : GlobalCache.eval(n.children()))
			nodeAdded(nn);
	}

	
	private void nodeRemoved(Node n) {
		//System.out.println (String.format("(NCT) node removed " + n + ": %d connections, preset size %d , postset size %d ", connections.get(n).size(), presets.get(n).size(), postsets.get(n).size()));
		
		for (Node postsetNodes: postsets.get(n))
			presets.get(postsetNodes).remove(n);

		for (Node presetNodes: presets.get(n))
			postsets.get(presetNodes).remove(n);
		
		removeHashes(n);
		
		if (n instanceof Connection) {
			Connection con = (Connection)n;
			Node c1 = con.getFirst();
			Node c2 = con.getSecond();

			LinkedHashSet<Node> set = postsets.get(c1);
			if (set != null)
				postsets.get(c1).remove(c2);

			set = presets.get(c2);
			if (set != null)
				presets.get(c2).remove(c1);

			LinkedHashSet<Connection> conSet = connections.get(c1);
			if (conSet != null)
				connections.get(c1).remove(con);
			conSet = connections.get(c2);
			if (conSet != null)
				connections.get(c2).remove(con);
		}
		
		for (Node nn : GlobalCache.eval(n.children()))
			nodeRemoved(nn);
	}

	public Set<Node> getPreset(Node node) {
		return Collections.unmodifiableSet(presets.get(node));
	}

	public Set<Node> getPostset(Node node) {
		return Collections.unmodifiableSet(postsets.get(node));
	}

	public Set<Connection> getConnections (Node node) {
		LinkedHashSet<Connection> result = connections.get(node);
		if(result == null)
			throw new RuntimeException("unknown node: " + node);
		return Collections.unmodifiableSet(result);		
	}

	@Override
	public void handleEvent(Collection<? extends Node> added, Collection<? extends Node> removed) {
		for(Node node : removed)
			nodeRemoved(node);
		for(Node node : added)
			nodeAdded(node);
	}

	@Override
	public NodeContext getState() {
		return this;
	}
}