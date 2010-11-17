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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.propertyeditor.Properties;

/**
 * A base class for all interpreted graph models. 
 * @author Ivan Poliakov
 *
 */
public abstract class AbstractModel implements Model {
	private NodeContextTracker nodeContextTracker;
	private ReferenceManager referenceManager;
	
	private String title = "";

	private Container root;

	public AbstractModel (Container root) {
		this (root, null);
	}
	
	public AbstractModel(Container root, ReferenceManager referenceManager) {
		this.root = root;
		this.referenceManager = (referenceManager == null) ? new DefaultReferenceManager(root) : referenceManager;
		nodeContextTracker = new NodeContextTracker(root);
	}
	
	public Model getMathModel() {
		return this;
	}

	public VisualModel getVisualModel() {
		return null;
	}
	
	public void add (Node node) {
		root.add(node);
	}

	public void remove (Node node) {
		remove(Arrays.asList(new Node[]{node}));
	}

	private void recursiveDelete(LinkedHashSet<Node> deletionList, Node node) {
		Collection<Node> dependants = getDependants(node);
		for(Node n : dependants)
			if(!deletionList.contains(n))
				recursiveDelete(deletionList, n);
		
		deletionList.add(node);
	}
	
	private Collection<Node> getDependants(Node node) {
		ArrayList<Node> result = new ArrayList<Node>();
		result.addAll(GlobalCache.eval(node.children()));
		result.addAll(getConnections(node));
		return result;
	}

	public void remove (Collection<Node> nodes) {
		LinkedHashSet<Node> deletionList = new LinkedHashSet<Node>(); 
		for(Node node : nodes)
			recursiveDelete(deletionList, node);
		
		for(Node n : deletionList) {
			Node parent = GlobalCache.eval(n.parent());
			if (parent instanceof Container)
				((Container)parent).remove(n);
			else
				;//throw new RuntimeException ("Cannot remove a child node from a node that is not a Container. The parent is: " + parent);
		}
	}

	public String getDisplayName() {
		DisplayName name = this.getClass().getAnnotation(DisplayName.class);
		if (name == null)
			return this.getClass().getSimpleName();
		else
			return name.value();
	}

	final public String getTitle() {
		return title;
	}

	final public void setTitle(String title) {
		this.title = title;
	}

	public final Container getRoot() {
		refreshStupidObservers();
		return root;	
	}

	public Set<Connection> getConnections(Node component) {
		refreshStupidObservers();
		return nodeContextTracker.getConnections(component);
	}

	public Set<Node> getPostset(Node component) {
		refreshStupidObservers();
		return nodeContextTracker.getPostset(component);
	}

	public Set<Node> getPreset(Node component) {
		refreshStupidObservers();
		return nodeContextTracker.getPreset(component);
	}
	
	@Override
	public Node getNodeByReference(String reference) {
		refreshStupidObservers();
		return referenceManager.getNodeByReference(reference);
	}

	@Override
	public String getNodeReference(Node node) {
		refreshStupidObservers();
		return referenceManager.getNodeReference(node);
	}
	
	@Override
	public Properties getProperties(Node node) {
		return null;
	}

	protected ReferenceManager getReferenceManager() {
		return referenceManager;
	}
	
	/**
	 * This should be called almost always. Subclasses should call refresh() to their HierarchySupervisors here.
	 */
	public void refreshStupidObservers() {
		//nodeContextTracker.refresh();
	}
}