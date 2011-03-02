package org.workcraft.gui.graph.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.util.Hierarchy;

import pcollections.HashTreePSet;

public class MiscellaneousModelActions {
	Collection<Node> selection;
	VisualModel model;
	
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			model.remove(selection);
		}

		if (!e.isCtrlDown())
		{
			if (!e.isShiftDown()) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					currentLevelUp(model);
					break;
				case KeyEvent.VK_PAGE_DOWN:
					currentLevelDown(model);
					break;
				case KeyEvent.VK_OPEN_BRACKET:
					VisualModelTransformer.rotateNodes(selection, model,-Math.PI/2);
					break;
				case KeyEvent.VK_CLOSE_BRACKET:
					VisualModelTransformer.rotateNodes(selection, model, Math.PI/2);
					break;
				case KeyEvent.VK_LEFT:
					VisualModelTransformer.translateNodes(selection, -1,0);
					break;
				case KeyEvent.VK_RIGHT:
					VisualModelTransformer.translateNodes(selection,  1,0);
					break;
				case KeyEvent.VK_UP:
					VisualModelTransformer.translateNodes(selection, 0,-1);
					break;
				case KeyEvent.VK_DOWN:
					VisualModelTransformer.translateNodes(selection, 0,1);
					break;
				}
			} else { // Shift is pressed

				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
					VisualModelTransformer.scaleNodes(selection, model,-1,1);
					break;
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
					VisualModelTransformer.scaleNodes(selection, model,1,-1);
					break;
				}
			}
		}

		if (e.isCtrlDown()) {
			switch(e.getKeyCode()){
			case KeyEvent.VK_G: 
				groupCollection(model, selection);
				break;
			case KeyEvent.VK_U:
				ungroup(model, selection);
				break;
			case KeyEvent.VK_C: 
				break;
			case KeyEvent.VK_X: 
				break;
			case KeyEvent.VK_V:
				break;
			case KeyEvent.VK_P:
				e.getEditor().getModelEntry().getStorage().checkpoint();
				break;
			case KeyEvent.VK_Z:
				e.getEditor().getModelEntry().getStorage().undo();
				break;
			case KeyEvent.VK_Y:
				e.getEditor().getModelEntry().getStorage().redo();
				break;
			}
		}
	}
	
	
	
	public static VisualGroup groupCollection(VisualModel model2, Collection<Node> selected) {

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
	public Collection<Node> ungroup(Collection<Node> nodes) {
		ArrayList<Node> toSelect = new ArrayList<Node>();
		
		for(Node node : nodes)
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
		
		return HashTreePSet.from(toSelect);
	}
	
	@Override
	public Node group(Collection<Node> nodes) {
		Collection<Node> selected = nodes;
		VisualGroup vg = groupCollection(selected);
		return vg;
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

	
	private ModifiableExpression<Container> currentLevel = new Variable<Container>(null){
		@Override
		public void setValue(Container value) {
//			selection.setValue(HashTreePSet.<Node>empty());
			super.setValue(value);
		};
	};

	
	
}
