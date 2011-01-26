package org.workcraft.plugins.cpog;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.Icon;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.util.GUI;

public class CustomToolsProvider implements
		org.workcraft.gui.graph.tools.CustomToolsProvider {

	private final class VertexGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/vertex.svg");

		@Override
		public String getLabel() {
			return "Vertex";
		}

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_V;
		}

		@Override
		public void generate(VisualModel model, Point2D where)
				throws NodeCreationException {
			VisualVertex vertex = ((VisualCPOG)model).createVertex();
			vertex.position().setValue(where);
		}
	}

	private final class VariableGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/variable.svg");

		@Override
		public String getLabel() {
			return "Variable";
		}

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_X;
		}

		@Override
		public void generate(VisualModel model, Point2D where)
				throws NodeCreationException {
			VisualVariable variable = ((VisualCPOG)model).createVariable();
			variable.position().setValue(where);
		}
	}

	private final class RhoClauseGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/rho.svg");

		@Override
		public String getLabel() {
			return "RhoClause";
		}

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_R;
		}

		@Override
		public void generate(VisualModel model, Point2D where)
				throws NodeCreationException {
			VisualRhoClause rhoClause = ((VisualCPOG)model).createRhoClause();
			rhoClause.position().setValue(where);
		}
	}

	@Override
	public Iterable<GraphEditorTool> getTools(GraphEditor editor)
	{
		ArrayList<GraphEditorTool> res = new ArrayList<GraphEditorTool>();
		
		res.add(new SelectionTool(editor));
		res.add(new ConnectionTool(editor));
		
		res.add(new NodeGeneratorTool(new VertexGenerator()));
		res.add(new NodeGeneratorTool(new VariableGenerator()));
		res.add(new NodeGeneratorTool(new RhoClauseGenerator()));
		
		return res;
	}

}
