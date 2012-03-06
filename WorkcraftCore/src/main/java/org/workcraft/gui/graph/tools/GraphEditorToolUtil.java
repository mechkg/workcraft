package org.workcraft.gui.graph.tools;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.GraphEditorTool.Button;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.util.Graphics;

public class GraphEditorToolUtil {

	public static GraphEditorTool attachPainter(final GraphEditorTool tool, final Expression<? extends GraphicalContent> painter) {
		return new GraphEditorTool() {
			@Override
			public Button getButton() {
				return tool.getButton();
			}
			
			@Override
			public GraphEditorKeyListener keyListener() {
				return tool.keyListener();
			}
			
			@Override
			public GraphEditorMouseListener mouseListener() {
				return tool.mouseListener();
			}

			public void activated() {
				tool.activated();
			}

			public void deactivated() {
				tool.deactivated();
			}

			public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
				return Graphics.compose(painter, tool.userSpaceContent(viewport, hasFocus));
			}

			public Expression<? extends GraphicalContent> screenSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
				return tool.screenSpaceContent(viewport, hasFocus);
			}

			public JPanel getInterfacePanel() {
				return tool.getInterfacePanel();
			}
		};
	}
	
	public static <D, T extends GraphEditorTool & DecorationProvider<D>> GraphEditorTool attachParameterisedPainter(final T tool, final Func<? super D, ? extends Expression<? extends GraphicalContent>> painterProvider) {
		return attachPainter(tool, painterProvider.eval(tool.getDecoration()));
	}
}
