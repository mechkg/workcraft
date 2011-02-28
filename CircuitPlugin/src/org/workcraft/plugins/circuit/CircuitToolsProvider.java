package org.workcraft.plugins.circuit;


import static java.util.Arrays.asList;
import static org.workcraft.gui.DefaultReflectiveModelPainter.reflectivePainterProvider;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachPainter;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachParameterisedPainter;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.tools.CircuitSelectionTool;
import org.workcraft.plugins.circuit.tools.CircuitSimulationTool;
import org.workcraft.plugins.circuit.tools.ContactGeneratorTool;
import org.workcraft.util.Func;
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
		final Func<Colorisator, Expression<? extends GraphicalContent>> colorisablePainter = reflectivePainterProvider(TouchableProvider.DEFAULT, editor.getModel().getRoot());
		final Expression<? extends GraphicalContent> simplePainter = colorisablePainter.eval(Colorisator.EMPTY);

		return asList(
				attachParameterisedPainter(new CircuitSelectionTool(editor), colorisablePainter),
				attachParameterisedPainter(new ConnectionTool(editor, TouchableProvider.DEFAULT), colorisablePainter),
				attachParameterisedPainter(new CircuitSimulationTool(editor), colorisablePainter),
		
				attachPainter(new ContactGeneratorTool(), simplePainter),
				attachPainter(new NodeGeneratorTool(new JointGenerator()), simplePainter),
				attachPainter(new NodeGeneratorTool(new FunctionComponentGenerator()), simplePainter));
	}

}
