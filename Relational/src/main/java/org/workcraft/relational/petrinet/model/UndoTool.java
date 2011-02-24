package org.workcraft.relational.petrinet.model;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.GraphEditor;

public class UndoTool extends AbstractTool {

	@Override
	public String getLabel() {
		return "Undo tool";
	}
	
	VisualModel relationalPetriNet;
	
	public void activated(GraphEditor editor) {
		relationalPetriNet = (VisualModel)editor.getModel();
	};
	
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		
		relationalPetriNet.data.undo();
		
		super.mouseClicked(e);
	}


	@Override
	public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
		return Expressions.constant(GraphicalContent.EMPTY);
	}

	@Override
	public Expression<? extends GraphicalContent> screenSpaceContent(Viewport view, Expression<Boolean> hasFocus) {
		return Expressions.constant(GraphicalContent.EMPTY);
	}

}
