package org.workcraft.plugins.petri;

import static org.workcraft.gui.DefaultReflectiveModelPainter.reflectivePainterProvider;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachPainter;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachParameterisedPainter;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.selection.SelectionTool;
import org.workcraft.gui.graph.tools.selection.SelectionToolConfig;
import org.workcraft.plugins.petri.tools.SimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;

public class PetriNetVisualModelDescriptor implements VisualModelDescriptor {
	
	@Override
	public VisualModel create(MathModel mathModel, StorageManager storage) throws VisualModelInstantiationException {
		return new VisualPetriNet ((PetriNet)mathModel, storage);
	}

	public Iterable<GraphEditorTool> createTools(VisualPetriNet model, GraphEditor editor) {
		TouchableProvider<Node> tp = TouchableProvider.DEFAULT;
		
		final Func<Colorisator, Expression<? extends GraphicalContent>> colorisablePainter = reflectivePainterProvider(tp, model.getRoot());
		
		return Arrays.asList(new GraphEditorTool[]{
			attachParameterisedPainter(new SelectionTool(new SelectionToolConfig.Default(model, tp)), colorisablePainter),
			attachParameterisedPainter(new ConnectionTool(editor, tp), colorisablePainter),
			attachPainter(new NodeGeneratorTool(new PlaceGenerator()), colorisablePainter.eval(Colorisator.EMPTY)),
			attachPainter(new NodeGeneratorTool(new TransitionGenerator()), colorisablePainter.eval(Colorisator.EMPTY)),
			attachParameterisedPainter(new SimulationTool(editor, tp), colorisablePainter),
		});
	}
	
}
