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

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

import pcollections.PVector;
import pcollections.TreePVector;

/**
 * A base class for all interpreted graph models. 
 * @author Ivan Poliakov
 *
 */
public abstract class AbstractModel implements Model {
	private Expression<? extends ReferenceManager> referenceManager;
	
	private String title = "";
	private Container root;
	
	final private HierarchyController hierarchyController;

	private final Expression<? extends NodeContext> nodeContext;

	public AbstractModel(ModelSpecification spec) {
		this.root = spec.root;
		this.referenceManager = spec.referenceManager;
		this.hierarchyController = spec.hierarchyController;
		this.nodeContext = spec.nodeContext;
	}

	public static class DefaultControllerChain {
		public DefaultControllerChain(HierarchyController hierarchyController, Expression<? extends NodeContext> nodeContextTracker) {
			this.hierarchyController = hierarchyController;
			this.nodeContextTracker = nodeContextTracker;
		}
		public final HierarchyController hierarchyController;
		public final Expression<? extends NodeContext> nodeContextTracker;
	}
	
	public static HierarchyController createDefaultControllerChain(Node root, final Expression<? extends NodeContext> context) {
		return new DependantRemovingHierarchyController(
				new DefaultHierarchyController(), new Func<Node, Collection<Node>>() {
					@Override
					public Collection<Node> eval(Node node) {
						ArrayList<Node> result = new ArrayList<Node>();
						result.addAll(GlobalCache.eval(node.children()));
						result.addAll(GlobalCache.eval(context).getConnections(node));
						return result;
					}
				}
			);
	}
	
	public static ModelSpecification createDefaultModelSpecification(Container root)
	{
		final Expression<NodeContext> nodeContext = createDefaultNodeContext(root);
		HierarchyController c = createDefaultControllerChain(root, nodeContext);
		Expression<ReferenceManager> rm = new HierarchySupervisor<ReferenceManager>(root, new DefaultReferenceManager());
		return new ModelSpecification(root, rm, c, nodeContext);
	}

	protected static HierarchySupervisor<NodeContext> createDefaultNodeContext(Container root) {
		return new HierarchySupervisor<NodeContext>(root, new NodeContextTracker());
	}
	
	public static ModelSpecification createDefaultModelSpecification(Container root, Expression<? extends ReferenceManager> rm)
	{
		final Expression<NodeContext> nodeContext = createDefaultNodeContext(root);
		HierarchyController c = createDefaultControllerChain(root, nodeContext);
		return new ModelSpecification(root, rm, c, nodeContext);
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
	public void remove (Node node) {
		hierarchyController.remove(node);
	}

	@Override
	public void remove (Collection<? extends Node> nodes) {
		for(Node node : nodes)
			if(Hierarchy.isDescendant(node, root))
				remove(node);
	}

	final public String getTitle() {
		return title;
	}

	final public void setTitle(String title) {
		this.title = title;
	}

	public Node getRoot() {
		return root;	
	}

	@Override
	public Expression<? extends ReferenceManager> referenceManager() {
		return referenceManager;
	}
	
	@Override
	public PVector<EditableProperty> getProperties(Node node) {
		return TreePVector.empty();
	}

	@Override
	public Expression<? extends NodeContext> nodeContext() {
		return nodeContext;
	}
}
