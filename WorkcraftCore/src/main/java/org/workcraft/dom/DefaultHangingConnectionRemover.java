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
import java.util.HashSet;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class DefaultHangingConnectionRemover extends HierarchySupervisor {
	private NodeContext nct;

	public DefaultHangingConnectionRemover (NodeContext nct, Node root) {
		super(root);
		this.nct = nct;
		start();
	}

	@Override
	public void handleEvent(List<Node> added, final List<Node> removed) {
		if(removed.size() > 0) {
	     	HashSet<Connection> hangingConnections = new HashSet<Connection>();
			
			Func<Connection, Boolean> hanging = new Func<Connection, Boolean>() {
				@Override
				public Boolean eval(Connection arg0) {
					return !isConnectionInside (removed, arg0); 
				}
			};
		
			for (Node node : removed)
				findHangingConnections(node, hangingConnections, hanging);
		
			for (Connection con : hangingConnections)
				if (GlobalCache.eval(con.parent()) instanceof Container)
					((Container)GlobalCache.eval(con.parent())).remove(con);
				else
					throw new RuntimeException ("Cannot remove a hanging connection because its parent is not a Container.");
		}
	}
	
	private static boolean isConnectionInside (Collection<Node> nodes, Connection con) {
		for (Node node : nodes)
			if (node == con || Hierarchy.isDescendant(con, node))
				return true;
		return false;
	}

	private void findHangingConnections(Node node, HashSet<Connection> hangingConnections, Func<Connection, Boolean> hanging) {
		// need only to remove those connections that are not already being deleted
		
		for (Connection con : nct.getConnections(node))
			if (hanging.eval(con))
				hangingConnections.add(con);
		for (Node nn : GlobalCache.eval(node.children()))
			findHangingConnections (nn, hangingConnections, hanging);
	}
}
