package org.workcraft.relational.petrinet.model;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.visual.SimpleGraphicalContent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
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
	public Expression<? extends Decorator> getDecorator() {
		return Expressions.constant(Decorator.Empty.INSTANCE);
	}

	@Override
	public Expression<? extends SimpleGraphicalContent> userSpaceContent() {
		return Expressions.constant(SimpleGraphicalContent.Empty.INSTANCE);
	}

	@Override
	public Expression<? extends SimpleGraphicalContent> screenSpaceContent(Viewport view) {
		return Expressions.constant(SimpleGraphicalContent.Empty.INSTANCE);
	}

}
