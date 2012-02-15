package org.workcraft.gui.graph.tools;

import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.graph.tools.GraphEditorTool.Button;

public class GraphEditorConfiguration {
	public final GraphEditorKeyListener keyListener;
	public final GraphEditorMouseListener mouseListener;

	public final Expression<? extends GraphicalContent> userSpaceContent;
	public final Expression<? extends GraphicalContent> screenSpaceContent;
	
	public final JPanel interfacePanel;

	public final Button button;

	public GraphEditorConfiguration(GraphEditorKeyListener keyListener,
			GraphEditorMouseListener mouseListener,
			Expression<? extends GraphicalContent> userSpaceContent,
			Expression<? extends GraphicalContent> screenSpaceContent,
			JPanel interfacePanel, Button button) {
		this.keyListener = keyListener;
		this.mouseListener = mouseListener;
		this.userSpaceContent = userSpaceContent;
		this.screenSpaceContent = screenSpaceContent;
		this.interfacePanel = interfacePanel;
		this.button = button;
	}
}