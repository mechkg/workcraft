package org.workcraft.plugins.stg;

import static org.workcraft.interop.LazyObjectProvider.asInitialiser;
import static org.workcraft.interop.LazyObjectProvider.lazy;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.graph.GraphEditable;
import org.workcraft.interop.ModelService;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ModelServicesImpl;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.interop.ServiceProviderImpl;
import org.workcraft.util.Function0;
import org.workcraft.util.Initialiser;

public class STGModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Signal Transition Graph";
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new STG(storage);
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return null;
	}

	@Override
	public ModelServices createServiceProvider(Model model, StorageManager storage) {
		if(model instanceof STG)
			return getServices((STG)model, (HistoryPreservingStorageManager)storage);
		else
			if(model instanceof VisualSTG)
				return getServices((VisualSTG)model, (HistoryPreservingStorageManager)storage);
		throw new NotSupportedException();
			//return ModelServicesImpl.createLegacyServiceProvider(model);
	}

	private static ModelServicesImpl getVisualServices(final Function0<VisualSTG> visualStg, HistoryPreservingStorageManager storage) {
		return ModelServicesImpl.EMPTY
		.plusDeferred(STGModel.SERVICE_HANDLE, new Initialiser<STGModel>(){
			@Override
			public STGModel create() {
				return visualStg.apply().stg;
			}})
		.plusDeferred(GraphEditable.SERVICE_HANDLE, new Initialiser<GraphEditable>() {
			@Override
			public GraphEditable create() {
				VisualSTG visualModel = visualStg.apply();
				return new StgGraphEditable(visualModel);
			}
		})
		.plusDeferred(ModelService.LegacyVisualModelService, asInitialiser(visualStg));
	}

	public static ModelServices getServices(VisualSTG model, HistoryPreservingStorageManager storage) {
		return getServices(model.stg, Function0.Util.constant(model), storage);
	}

	public static ModelServices getServices(final STG model, final HistoryPreservingStorageManager storage) {
		return getServices(model, lazy(new Initialiser<VisualSTG>(){
			@Override
			public VisualSTG create() {
				return new VisualSTG(model, storage);
			}}), storage);
	}
	
	private static ModelServices getServices(final STG stg, final Function0<VisualSTG> visualStg, final HistoryPreservingStorageManager storage) {
		return ModelServicesImpl.EMPTY
		.plus(ModelDescriptor.SERVICE_HANDLE, new STGModelDescriptor())
		.plus(STGModel.SERVICE_HANDLE, stg)
		.plus(HistoryPreservingStorageManager.SERVICE_HANDLE, storage)
		.plus(ModelService.LegacyMathModelService, stg)
		.plusAll(getVisualServices(visualStg, storage));
	}
}
