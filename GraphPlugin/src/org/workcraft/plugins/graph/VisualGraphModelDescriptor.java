package org.workcraft.plugins.graph;

import java.util.Collections;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class VisualGraphModelDescriptor implements VisualModelDescriptor {

	@Override
	public VisualModel create(MathModel mathModel, StorageManager storage)
			throws VisualModelInstantiationException {
		return new VisualGraph((Graph)mathModel, storage);
	}

	@Override
	public Iterable<? extends GraphEditorTool> createTools(GraphEditor editor) {
		return Collections.emptySet();
	}
}
