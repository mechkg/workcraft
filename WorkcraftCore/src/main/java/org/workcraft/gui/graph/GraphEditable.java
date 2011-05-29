package org.workcraft.gui.graph;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.interop.ModelService;

import pcollections.PVector;

public interface GraphEditable {

	ModelService<GraphEditable> SERVICE_HANDLE = ModelService.createNewService(GraphEditable.class, "The client of a GraphEditorPanel");

	public Iterable<? extends GraphEditorTool> createTools(GraphEditor editor);
	public Expression<? extends PVector<EditableProperty>> properties();
}
