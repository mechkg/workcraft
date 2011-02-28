package org.workcraft.workspace;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;

public class ModelEntry {
	private final ModelDescriptor descriptor;
	private Model model;
	public final ServiceProvider services;
	private final HistoryPreservingStorageManager storage;

	public ModelEntry(ModelDescriptor descriptor, Model model, StorageManager storage) {
		this(descriptor, model, storage, descriptor.createServiceProvider(model));
	}
	
	public ModelEntry(ModelDescriptor descriptor, Model model, StorageManager storage, ServiceProvider services)
	{
		this.descriptor = descriptor;
		this.model = model;
		this.services = services;
		this.storage = storage instanceof HistoryPreservingStorageManager ? (HistoryPreservingStorageManager) storage : null;
	}

	public ModelDescriptor getDescriptor() {
		return descriptor;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}
	
	public VisualModel getVisualModel() {
		if (isVisual())
			return (VisualModel) model;
		else
			return null;
	}
	
	public MathModel getMathModel() {
		if (isVisual())
			return getVisualModel().getMathModel();
		else
			return (MathModel)model;
	}
	
	public boolean isVisual() {
		return model instanceof VisualModel;
	}
	
	public HistoryPreservingStorageManager getStorage() {
		return storage;
	}
}
