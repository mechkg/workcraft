package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class STGVisualModelDescriptor implements VisualModelDescriptor {

	@Override
	public VisualModel create(MathModel mathModel, StorageManager storage) throws VisualModelInstantiationException {
		return new VisualSTG ((STG) mathModel, storage);
	}

	
	@Override
	public Iterable<GraphEditorTool> createTools(GraphEditor editor) {
		return new STGToolsProvider().getTools(editor);
	}

}
