package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.interop.ModelService;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;

public interface STGModel extends PetriNetModel {
	public static ModelService<STGModel> SERVICE_HANDLE = ModelService.createNewService(STGModel.class, "STG representation of the underlying model");
	
	public SignalTransition createSignalTransition (String name);
	
	public Collection<? extends SignalTransition> getSignalTransitions();
	public Collection<? extends SignalTransition> getSignalTransitions(Type type);
	
	public Collection<? extends Transition> getDummies();
	
	public Set<String> getDummyNames();
	public Set<String> getSignalNames (Type type);
	
	public Expression<? extends Direction> direction(SignalTransition transition);
}
