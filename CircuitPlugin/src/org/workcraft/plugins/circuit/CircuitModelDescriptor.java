package org.workcraft.plugins.circuit;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class CircuitModelDescriptor implements ModelDescriptor {

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new Circuit(storage);
	}

	@Override
	public String getDisplayName() {
		return "Digital Circuit";
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualModelDescriptor()
		{
			@Override
			public VisualModel create(MathModel mathModel, StorageManager storage)
					throws VisualModelInstantiationException {
				return new VisualCircuit((Circuit)mathModel, storage);
			}
			
			@Override
			public Iterable<GraphEditorTool> createTools(GraphEditor editor) {
				return new CircuitToolsProvider().getTools(editor);
			}
		};
	}

}
