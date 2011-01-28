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

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.references.AbstractReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.util.Func;

/**
 * A base class for all interpreted graph models. 
 * @author Ivan Poliakov
 *
 */
public abstract class AbstractModel implements Model {
	private ReferenceManager referenceManager;
	
	private String title = "";
	private Container root;
	
	final private HierarchyController hierarchyController;

	private final NodeContext nodeContext;

	public AbstractModel(ModelSpecification spec) {
		this.root = spec.root;
		this.referenceManager = spec.referenceManager;
		this.hierarchyController = spec.hierarchyController;
		this.nodeContext = spec.nodeContext;
	}

	public static class DefaultControllerChain {
		public DefaultControllerChain(HierarchyController hierarchyController, NodeContextTracker nodeContextTracker) {
			this.hierarchyController = hierarchyController;
			this.nodeContextTracker = nodeContextTracker;
		}
		public final HierarchyController hierarchyController;
		public final NodeContextTracker nodeContextTracker;
	}
	
	public static DefaultControllerChain createDefaultControllerChain(Node root) {
		final NodeContextTracker nodeContextTracker = new NodeContextTracker(root);
		return new DefaultControllerChain(new StinkyHierarchyController(
		new DependantRemovingHierarchyController(
				new DefaultHierarchyController(), new Func<Node, Collection<Node>>() {
						@Override
						public Collection<Node> eval(Node node) {
							ArrayList<Node> result = new ArrayList<Node>();
							result.addAll(GlobalCache.eval(node.children()));
							result.addAll(nodeContextTracker.getConnections(node));
							return result;
						}
					}
				),
				nodeContextTracker), nodeContextTracker);
	}
	
	public static ModelSpecification createDefaultModelSpecification(Container root)
	{
		DefaultControllerChain c = createDefaultControllerChain(root);
		DefaultReferenceManager rm = new DefaultReferenceManager(root);
		return new ModelSpecification(root, rm, new StinkyHierarchyController(c.hierarchyController, rm), c.nodeContextTracker);
	}
	
	public static ModelSpecification createDefaultModelSpecification(Container root, AbstractReferenceManager rm)
	{
		DefaultControllerChain c = createDefaultControllerChain(root);
		return new ModelSpecification(root, rm, new StinkyHierarchyController(c.hierarchyController, rm), c.nodeContextTracker);
	}
	
	public Model getMathModel() {
		return this;
	}

	public VisualModel getVisualModel() {
		return null;
	}
	
	@Override
	final public void add (Node node) {
		add(root, node);
	}

	@Override
	final public void add (Container parent, Node node) {
		hierarchyController.add(parent, node);
	}

	@Override
	final public void remove (Node node) {
		hierarchyController.remove(node);
	}

	@Override
	final public void remove (Collection<Node> nodes) {
		for(Node node : nodes)
			remove(node);
	}

	final public String getTitle() {
		return title;
	}

	final public void setTitle(String title) {
		this.title = title;
	}

	public final Node getRoot() {
		ensureConsistency();
		return root;	
	}

	@Override
	public Node getNodeByReference(String reference) {
		ensureConsistency();
		return referenceManager.getNodeByReference(reference);
	}

	@Override
	public String getNodeReference(Node node) {
		ensureConsistency();
		return referenceManager.getNodeReference(node);
	}
	
	@Override
	public Properties getProperties(Node node) {
		return null;
	}

	protected ReferenceManager getReferenceManager() {
		return referenceManager;
	}
	
	@Override
	public NodeContext getNodeContext() {
		return nodeContext;
	}
	
	/**
	 * This should be called almost always. Subclasses should call refresh() to their HierarchySupervisors here.
	 */
	public void ensureConsistency() {
		//nodeContextTracker.refresh();
	}
}