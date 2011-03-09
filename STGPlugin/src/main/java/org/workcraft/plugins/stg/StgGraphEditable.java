package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.GraphEditable;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.util.Function;

import pcollections.PSet;
import pcollections.PVector;
import pcollections.TreePVector;

public class StgGraphEditable implements GraphEditable {

	STGToolsProvider toolsProvider;
	StgEditorState editorState;
	Expression<? extends PVector<EditableProperty>> properties;

	public StgGraphEditable(final VisualSTG stg) {
		editorState = new StgEditorState(stg.getRoot());
		properties = bindFunc(editorState.selection, new Function<PSet<Node>, PVector<EditableProperty>>(){
			@Override
			public PVector<EditableProperty> apply(PSet<Node> argument) {
				if(argument.size() == 1) {
					return stg.getProperties(argument.iterator().next());
				} else
					return TreePVector.empty();
			}
		});
		toolsProvider = new STGToolsProvider(stg, editorState);
	}
	
	@Override
	public Iterable<? extends GraphEditorTool> createTools(GraphEditor editor) {
		return toolsProvider.getTools(editor);
	}

	@Override
	public Expression<? extends PVector<EditableProperty>> properties() {
		return properties;
	}

}
