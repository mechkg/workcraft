package org.workcraft.gui.graph;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.interop.ServiceHandle;

import pcollections.PVector;

public interface GraphEditable {

	ServiceHandle<GraphEditable> SERVICE_HANDLE = ServiceHandle.createNewService(GraphEditable.class, "The client of a GraphEditorPanel");

	public Iterable<? extends GraphEditorTool> createTools(GraphEditor editor);
	public Expression<? extends PVector<EditableProperty>> properties();
}
