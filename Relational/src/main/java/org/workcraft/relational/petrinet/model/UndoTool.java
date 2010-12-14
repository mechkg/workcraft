package org.workcraft.relational.petrinet.model;

import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decorator;
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
	public Decorator getDecorator() {
		return Decorator.Empty.INSTANCE;
	}

}
