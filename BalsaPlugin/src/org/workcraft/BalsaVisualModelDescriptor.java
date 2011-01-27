package org.workcraft;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.VisualBalsaCircuit;

public class BalsaVisualModelDescriptor implements VisualModelDescriptor {

	@Override
	public VisualModel create(MathModel mathModel, StorageManager storage) throws VisualModelInstantiationException {
		return new VisualBalsaCircuit((BalsaCircuit) mathModel, storage);
	}

	@Override
	public Iterable<GraphEditorTool> createTools(GraphEditor editor) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
