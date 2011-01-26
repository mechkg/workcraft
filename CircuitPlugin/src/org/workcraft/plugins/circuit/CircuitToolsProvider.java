package org.workcraft.plugins.circuit;


import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.Icon;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.tools.CircuitSelectionTool;
import org.workcraft.plugins.circuit.tools.CircuitSimulationTool;
import org.workcraft.plugins.circuit.tools.ContactGeneratorTool;
import org.workcraft.util.GUI;

public class CircuitToolsProvider implements CustomToolsProvider {
	
	private final class JointGenerator implements NodeGenerator {
		private Icon icon = GUI.createIconFromSVG("images/icons/svg/circuit-joint.svg");

		@Override
		public String getLabel() {
			return "Joint";
		}

		@Override
		public Icon getIcon() {
			return icon ;
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_J;
		}

		@Override
		public void generate(VisualModel model, Point2D where)
				throws NodeCreationException {
			((VisualCircuit)model).createJoint(where);
		}
	}
	
	private final class FunctionComponentGenerator implements NodeGenerator {

		private Icon icon = GUI.createIconFromSVG("images/icons/svg/circuit-formula.svg");

		@Override
		public String getLabel() {
			return "Function";
		}

		@Override
		public Icon getIcon() {
			return icon ;
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_F;
		}

		@Override
		public void generate(VisualModel model, Point2D where)
				throws NodeCreationException {
			((VisualCircuit)model).createFunctionComponent(where);
		}
	}

	@Override
	public Iterable<GraphEditorTool> getTools(GraphEditor editor) {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();
		
		result.add(new CircuitSelectionTool(editor));
		result.add(new ConnectionTool(editor));
		result.add(new CircuitSimulationTool(editor));
		result.add(new ContactGeneratorTool());
		result.add(new NodeGeneratorTool(new JointGenerator()));
		result.add(new NodeGeneratorTool(new FunctionComponentGenerator()));
		
		return result;
	}

}
