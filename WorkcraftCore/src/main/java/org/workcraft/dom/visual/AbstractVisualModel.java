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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.NodeFactory;
import org.workcraft.annotations.MouseListeners;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
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
import org.workcraft.exceptions.PasteException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.XmlUtil;

import pcollections.HashTreePSet;
import pcollections.PSet;

@MouseListeners ({ DefaultAnchorGenerator.class })
public abstract class AbstractVisualModel extends AbstractModel implements VisualModel {
	private MathModel mathModel;
	private ModifiableExpression<Container> currentLevel = new Variable<Container>(null){
		@Override
		public void setValue(Container value) {
			selection.setValue(HashTreePSet.<Node>empty());
			super.setValue(value);
		};
	};
	private final ModifiableExpression<PSet<Node>> selection;

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
					new SelectionEventPropagator(selection,
							new TeeHierarchyController(defaultSpec.hierarchyController,
							new DefaultMathNodeRemover(this.root, mathModel))
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
		selection = storage.<PSet<Node>>create(HashTreePSet.<Node>empty());
		
		currentLevel.setValue(param.root);
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
	public ModifiableExpression<PSet<Node>> selection() {
		return selection;
	}

	/**
	 * Select all components, connections and groups from the <code>root</code> group.
	 */
/*	@Override
	public void selectAll() {
		Collection<? extends Node> children = GlobalCache.eval(getRoot().children());
		if(GlobalCache.eval(selection).size()==children.size())
			return;
		
		selection.clear();
		selection.addAll(children);
	}*/

	private void validateSelection (Node node) {
		if (!Hierarchy.isDescendant(node, eval(currentLevel())))
			throw new RuntimeException ("Cannot select a node that is not in the current editing level (" + node + "), parent (" + GlobalCache.eval(node.parent()) +")");
	}

	public boolean isSelected(Node node) {
		return GlobalCache.eval(selection).contains(node);
	}

	@Override
	public MathModel getMathModel() {
		return mathModel;
	}

	@Override
	public VisualModel getVisualModel() {
		return this;
	}
	
	public Collection<Node> getOrderedCurrentLevelSelection() {
		List<Node> result = new ArrayList<Node>();
		for(Node node : GlobalCache.eval(eval(currentLevel).children()))
		{
			if(GlobalCache.eval(selection).contains(node) && node instanceof VisualNode)
				result.add((VisualNode)node);
		}
		return result;
	}

	public ModifiableExpression<Container> currentLevel() {
		return currentLevel;
	}

	private Collection<Node> getGroupableSelection()
	{
		ArrayList<Node> result = new ArrayList<Node>();
		for(Node node : getOrderedCurrentLevelSelection())
			if(node instanceof VisualTransformableNode)
				result.add((VisualTransformableNode)node);
		return result;
	}

	
	/**
	 * Groups the selection, and selects the newly created group.
	 * @author Arseniy Alekseyev
	 */
	@Override
	public void groupSelection() {
		Collection<Node> selected = getGroupableSelection();
		VisualGroup vg = groupCollection(selected);
		if (vg!=null) selection.setValue(HashTreePSet.<Node>singleton(vg));
	}
	
	protected final StorageManager storage;
	
	public VisualGroup groupCollection(Collection<Node> selected) {

		if(selected.size() <= 1)
			return null;
		
		VisualGroup group = new VisualGroup(storage);
		
		Container currentLevel = eval(this.currentLevel);

		currentLevel.add(group);

		currentLevel.reparent(selected, group);

		ArrayList<Node> connectionsToGroup = new ArrayList<Node>();

		for(VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class))
		{
			if(Hierarchy.isDescendant(connection.getFirst(), group) && 
					Hierarchy.isDescendant(connection.getSecond(), group)) {
				connectionsToGroup.add(connection);
			}
		}
		
		currentLevel.reparent(connectionsToGroup, group);
		
		return group;
		
	}

	/**
	 * Ungroups all groups in the current selection and adds the ungrouped components to the selection.
	 * @author Arseniy Alekseyev
	 */
	@Override
	public void ungroupSelection() {
		ArrayList<Node> toSelect = new ArrayList<Node>();
		
		for(Node node : getOrderedCurrentLevelSelection())
		{
			if(node instanceof VisualGroup)
			{
				VisualGroup group = (VisualGroup)node;
				for(Node subNode : group.unGroup())
					toSelect.add(subNode);
				eval(currentLevel).remove(group);
			}
			else
				toSelect.add(node);
		}
		
		selection.setValue(HashTreePSet.from(toSelect));
	}

	@Override
	public void deleteSelection() {
		remove(eval(selection()));
	}

	/**
	 * @param clipboard
	 * @param clipboardOwner
	 * @author Ivan Poliakov
	 * @throws ParserConfigurationException 
	 */
	public void copy(Clipboard clipboard, ClipboardOwner clipboardOwner) throws ParserConfigurationException {
		Document doc = XmlUtil.createDocument();

		Element root = doc.createElement("workcraft-clipboard-contents");

		doc.appendChild(root);
		root = doc.getDocumentElement();
		//selectionToXML(root);		
		clipboard.setContents(new TransferableDocument(doc), clipboardOwner);
	}

	public Collection<Node> paste(Collection<Node> what, Point2D where) throws PasteException {
		/*try {
			Document doc = (Document)clipboard.getData(TransferableDocument.DOCUMENT_FLAVOR);

			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals("workcraft-clipboard-contents"))
				return null;

			Element mathModelElement = XmlUtil.getChildElement("model", root);
			Element visualModelElement = XmlUtil.getChildElement("visual-model", root);

			if (mathModelElement == null || visualModelElement == null)
				throw new PasteException("Structure of clipboard XML is invalid.");

			mathModel.pasteFromXML(mathModelElement);
			//return pasteFromXML(visualModelElement, where);			 
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
			throw new PasteException (e);
		} catch (LoadFromXMLException e) {
			throw new PasteException (e);
		}*/

		return null;
	}



	public void cut(Clipboard clipboard, ClipboardOwner clipboardOwner) throws ParserConfigurationException {
		copy(clipboard, clipboardOwner);
		deleteSelection();
	}

	@Override public Properties getProperties(Node node) {
		return null;
	}
	
}
