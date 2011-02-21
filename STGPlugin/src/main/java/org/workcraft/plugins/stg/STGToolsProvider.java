package org.workcraft.plugins.stg;

import static java.util.Arrays.asList;
import static org.workcraft.gui.DefaultReflectiveModelPainter.reflectivePainterProvider;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachPainter;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachParameterisedPainter;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;

public class STGToolsProvider implements CustomToolsProvider {

	private final static class PlaceGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/place.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Place";
		}

		@Override
		public void generate(VisualModel model, Point2D where) throws NodeCreationException {
			VisualPlace place = ((VisualSTG)model).createPlace();
			place.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_P;
		}
	}

	private final static class SignalTransitionGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/signal-transition.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Signal Transition";
		}

		@Override
		public void generate(VisualModel model, Point2D where) throws NodeCreationException {
			VisualSignalTransition transition = ((VisualSTG)model).createSignalTransition();
			transition.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_T;
		}
	}
	
	private final static class DummyTransitionGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/transition.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Dummy Transition";
		}

		@Override
		public void generate(VisualModel model, Point2D where) throws NodeCreationException {
			VisualDummyTransition transition = ((VisualSTG)model).createDummyTransition();
			transition.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_D;
		}
	}
	
	@Override
	public Iterable<GraphEditorTool> getTools(GraphEditor editor) {
		final Func<Colorisator, Expression<? extends GraphicalContent>> painterProvider = reflectivePainterProvider(editor.getModel().getRoot());
		final Expression<? extends GraphicalContent> simpleModelPainter = painterProvider.eval(Colorisator.EMPTY);
		
		return asList(
				attachParameterisedPainter(new STGSelectionTool(editor), painterProvider),
				attachParameterisedPainter(new ConnectionTool(editor), painterProvider),
				attachPainter(new NodeGeneratorTool(new PlaceGenerator()), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new SignalTransitionGenerator()), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new DummyTransitionGenerator()), simpleModelPainter),
				attachParameterisedPainter(new STGSimulationTool(editor), painterProvider));
	}

}
