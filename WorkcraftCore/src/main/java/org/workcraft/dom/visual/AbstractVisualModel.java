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

import java.util.HashMap;

import org.workcraft.NodeFactory;
import org.workcraft.annotations.MouseListeners;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.DefaultMathNodeRemover;
import org.workcraft.dom.ModelSpecification;
import org.workcraft.dom.Node;
import org.workcraft.dom.TeeHierarchyController;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.DefaultAnchorGenerator;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.Properties;

import pcollections.HashTreePSet;
import pcollections.PSet;
import pcollections.PVector;
import pcollections.TreePVector;

@MouseListeners ({ DefaultAnchorGenerator.class })
public abstract class AbstractVisualModel extends AbstractModel implements VisualModel {
	private MathModel mathModel;

	public AbstractVisualModel(VisualGroup root, StorageManager storage) {
		this (null, root, storage);
	}

	public AbstractVisualModel(StorageManager storage) {
		this ((MathModel)null, storage);
	}

	public AbstractVisualModel(MathModel mathModel, StorageManager storage) {
		this(mathModel, null, storage);
	}
	
	static class ConstructionInfo {
		public ConstructionInfo(MathModel mathModel, VisualGroup root, StorageManager storage) {
			this.mathModel = mathModel;
			this.storage = storage;
			this.root = root == null ? new VisualGroup(storage) : root;
			ModelSpecification defaultSpec = createDefaultModelSpecification(this.root);
			ModifiableExpression<PSet<Node>> rawSelection = storage.<PSet<Node>>create(HashTreePSet.<Node>empty());
			
			selection = Expressions.modifiableExpression(new RemovedNodeDeselector(this.root, rawSelection), rawSelection);
			this.spec = new ModelSpecification(defaultSpec.root, defaultSpec.referenceManager, 
						new TeeHierarchyController(defaultSpec.hierarchyController,
							new DefaultMathNodeRemover(this.root, mathModel)
					), defaultSpec.nodeContext);
		}
		final ModelSpecification spec;
		final ModifiableExpression<PSet<Node>> selection;
		
		final MathModel mathModel; 
		final VisualGroup root;
		final StorageManager storage;
	}

	public AbstractVisualModel(MathModel mathModel, VisualGroup root, StorageManager storage) {
		this(new ConstructionInfo(mathModel, root, storage));
	}
	
	public AbstractVisualModel(ConstructionInfo param) {
		super(param.spec);
		this.mathModel = param.mathModel;
		this.storage = param.storage;
	}

	protected final void createDefaultFlatStructure() throws NodeCreationException {
		HashMap <MathNode, VisualComponent> createdNodes = new HashMap <MathNode, VisualComponent>();
		HashMap <VisualConnection, MathConnection> createdConnections = new	HashMap <VisualConnection, MathConnection>();

		for (Node n : GlobalCache.eval(mathModel.getRoot().children())) {
			if (n instanceof MathConnection) {
				MathConnection connection = (MathConnection)n;

				// Will create incomplete instance, setConnection() needs to be called later to finalise.
				// This is to avoid cross-reference problems.
				VisualConnection visualConnection = NodeFactory.createVisualConnection(connection, storage);
				createdConnections.put(visualConnection, connection);
			} else {
				MathNode node = (MathNode)n;
				VisualComponent visualComponent = (VisualComponent)NodeFactory.createVisualComponent(node, storage);

				if (visualComponent != null) {
					add(visualComponent);
					createdNodes.put(node, visualComponent);
				}
			}
		}

		for (VisualConnection vc : createdConnections.keySet()) {
			MathConnection mc = createdConnections.get(vc);
			vc.setVisualConnectionDependencies(createdNodes.get(mc.getFirst()), 
					createdNodes.get(mc.getSecond()), new Polyline(vc, storage), mc);
			add(vc);
		}
	}

	@Override
	public MathModel getMathModel() {
		return mathModel;
	}

	@Override
	public VisualModel getVisualModel() {
		return this;
	}
	
	protected final StorageManager storage;

	@Override public PVector<EditableProperty> getProperties(Node node) {
		if(node instanceof Properties)
			return ((Properties)node).getProperties();
		else
			return TreePVector.empty();
	}
}
