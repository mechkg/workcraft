package org.workcraft.plugins.petri;

import java.util.Arrays;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.tools.SimulationTool;

public class PetriNetVisualModelDescriptor implements VisualModelDescriptor {

	@Override
	public VisualModel create(MathModel mathModel, StorageManager storage) throws VisualModelInstantiationException {
		return new VisualPetriNet ((PetriNet)mathModel);
	}

	@Override
	public Iterable<GraphEditorTool> createTools(GraphEditor editor) {
		return Arrays.asList(new GraphEditorTool[]{
				new SelectionTool(editor),
				new ConnectionTool(editor),
				new NodeGeneratorTool(new DefaultNodeGenerator(Place.class)),
				new NodeGeneratorTool(new DefaultNodeGenerator(Transition.class)),
				new SimulationTool(editor)
		});
	}
}
