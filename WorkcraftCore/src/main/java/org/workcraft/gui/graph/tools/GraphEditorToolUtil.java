package org.workcraft.gui.graph.tools;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.util.Func;
import org.workcraft.util.Graphics;

public class GraphEditorToolUtil {

	public static GraphEditorTool attachPainter(final GraphEditorTool tool, final Expression<? extends GraphicalContent> painter) {
		return new GraphEditorTool() {

			public void keyTyped(GraphEditorKeyEvent event) {
				tool.keyTyped(event);
			}

			public void mouseMoved(GraphEditorMouseEvent e) {
				tool.mouseMoved(e);
			}

			public void keyPressed(GraphEditorKeyEvent event) {
				tool.keyPressed(event);
			}

			public void mouseClicked(GraphEditorMouseEvent e) {
				tool.mouseClicked(e);
			}

			public void keyReleased(GraphEditorKeyEvent event) {
				tool.keyReleased(event);
			}

			public void mouseEntered(GraphEditorMouseEvent e) {
				tool.mouseEntered(e);
			}

			public void mouseExited(GraphEditorMouseEvent e) {
				tool.mouseExited(e);
			}

			public void activated() {
				tool.activated();
			}

			public void mousePressed(GraphEditorMouseEvent e) {
				tool.mousePressed(e);
			}

			public void deactivated() {
				tool.deactivated();
			}

			public Expression<? extends GraphicalContent> userSpaceContent(Expression<Boolean> hasFocus) {
				return Graphics.compose(painter, tool.userSpaceContent(hasFocus));
			}

			public void mouseReleased(GraphEditorMouseEvent e) {
				tool.mouseReleased(e);
			}

			public void startDrag(GraphEditorMouseEvent e) {
				tool.startDrag(e);
			}

			public Expression<? extends GraphicalContent> screenSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
				return tool.screenSpaceContent(viewport, hasFocus);
			}

			public void finishDrag(GraphEditorMouseEvent e) {
				tool.finishDrag(e);
			}

			public boolean isDragging() {
				return tool.isDragging();
			}

			public JPanel getInterfacePanel() {
				return tool.getInterfacePanel();
			}

			public String getLabel() {
				return tool.getLabel();
			}

			public Icon getIcon() {
				return tool.getIcon();
			}

			public int getHotKeyCode() {
				return tool.getHotKeyCode();
			}
		};
	}
	
	public static <D, T extends GraphEditorTool & DecorationProvider<D>> GraphEditorTool attachParameterisedPainter(final T tool, final Func<? super D, ? extends Expression<? extends GraphicalContent>> painterProvider) {
		return attachPainter(tool, painterProvider.eval(tool.getDecoration()));
	}
}
