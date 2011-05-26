package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static pcollections.TreePVector.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.workcraft.Framework;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.graph.GraphEditable;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.interop.ServiceProviderImpl;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;

import pcollections.PVector;

public class CpogModelDescriptor implements ModelDescriptor {

	@Override
	public String getDisplayName() {
		return "Conditional Partial Order Graph";
	}

	@Override
	public ServiceProvider newDocument() {
		final CPOG cpog = new CPOG(new HistoryPreservingStorageManager());
		
		return ServiceProviderImpl.EMPTY
			.plus(GraphEditable.SERVICE_HANDLE, new GraphEditable() {
				
				@Override
				public Expression<? extends PVector<EditableProperty>> properties() {
					PVector<EditableProperty> empty = empty();
					return constant(empty);
				}
				
				@Override
				public Iterable<? extends GraphEditorTool> createTools(GraphEditor editor) {
					return org.workcraft.plugins.cpog.scala.ToolsProvider.getTools(cpog, editor.snapFunction());
				}
			})
			.plus(Framework.CustomSaver.SERVICE_HANDLE, new Framework.CustomSaver(){

				@Override
				public void write(OutputStream out) {
					try {
						new ObjectOutputStream(out).writeObject(org.workcraft.plugins.cpog.scala.nodes.snapshot.SnapshotMaker.makeSnapshot(cpog.nodes()));
					}
					catch(IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
	}

	@Override
	public ServiceProvider createServiceProvider(Model model, StorageManager storage) {
		throw new NotSupportedException();
	}
}
