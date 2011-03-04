package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.gui.graph.GraphEditable;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.EditableProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class StgGraphEditable implements GraphEditable {

	STGToolsProvider toolsProvider;

	public StgGraphEditable(VisualSTG stg) {
		toolsProvider = new STGToolsProvider(stg);
	}
	
	@Override
	public Iterable<? extends GraphEditorTool> createTools(GraphEditor editor) {
		return toolsProvider.getTools(editor);
	}

	@Override
	public Expression<? extends PVector<EditableProperty>> properties() {
		// TODO Auto-generated method stub
		return Expressions.constant(TreePVector.<EditableProperty>empty());
	}

}
