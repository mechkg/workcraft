package org.workcraft.relational.petrinet.model;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.DefaultReflectiveModelPainter;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.GraphEditorToolUtil;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.gui.graph.tools.SelectionToolConfig;

import pcollections.TreePVector;

public class RelationalModule implements Module {

	@Override
	public String getDescription() {
		return "Relational petri net data model";
	}

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, new Initialiser<ModelDescriptor>(){
			public ModelDescriptor create() {
				return new ModelDescriptor(){

					@Override
					public String getDisplayName() {
						return "Relational petri net";
					}

					@Override
					public MathModel createMathModel(StorageManager storage) {
						return null;
					}

					@Override
					public VisualModelDescriptor getVisualModelDescriptor() {
						return new VisualModelDescriptor(){

							@Override
							public VisualModel create(MathModel mathModel, StorageManager storage)
									throws VisualModelInstantiationException {
								return new org.workcraft.relational.petrinet.model.VisualModel();
							}

							@Override
							public Iterable<? extends GraphEditorTool> createTools(GraphEditor editor) {
								return TreePVector.<GraphEditorTool>singleton(
										GraphEditorToolUtil.attachParameterisedPainter(new SelectionTool(new SelectionToolConfig.Default(editor.getModel())), DefaultReflectiveModelPainter.reflectivePainterProvider(editor.getModel().getRoot()))
								).plus(new UndoTool());
							}
							
						};
					}
					
				};
				
			};
		});
	}
	
}
