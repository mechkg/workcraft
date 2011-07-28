package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.interop.ModelService;

public class HistoryPreservingStorageManager implements StorageManager {

	public static final ModelService<HistoryPreservingStorageManager> SERVICE_HANDLE = ModelService.createNewService(HistoryPreservingStorageManager.class, "History preserving storage manager");
	HashSet<ModifiableExpression<?>> allVars = new HashSet<ModifiableExpression<?>>();
	//private Map<ModifiableExpression<?>, Object> savedState;
	
	public Map<ModifiableExpression<?>, Object> save() {
		HashMap<ModifiableExpression<?>, Object> result = new HashMap<ModifiableExpression<?>, Object>();
		for(ModifiableExpression<?> var : allVars)
			result.put(var, eval(var));
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void restore(Map<ModifiableExpression<?>, Object> values) {
		for(ModifiableExpression<?> expr : values.keySet()) {
			((ModifiableExpression<Object>)expr).setValue(values.get(expr));
		}
	}
	
	@Override
	public <T> ModifiableExpression<T> create(T initialValue) {
		Variable<T> var = Variable.create(initialValue);
		allVars.add(var);
		return var;
	}

	/*public void dosave() {
		System.out.println("saving");
		savedState = save();
	}

	public void dorestore() {
		System.out.println("restoring");
		restore(savedState);
	}*/

	ArrayList<Map<ModifiableExpression<?>, Object>> history = new ArrayList<Map<ModifiableExpression<?>, Object>>();
	int historyPosition = -1;
	
	public void checkpoint() {
		Map<ModifiableExpression<?>, Object> newState = save();
		if(historyPosition == -1 || !stateEquals(newState, history.get(historyPosition))){
			System.out.println("new state!");
			for(int i=history.size()-1;i>historyPosition;i--)
				history.remove(i);
			
			history.add(newState);
			historyPosition = history.size()-1;
		}
	}
	
	private boolean stateEquals(Map<ModifiableExpression<?>, Object> state1,
			Map<ModifiableExpression<?>, Object> state2) {
		for(ModifiableExpression<?> expr : allVars) {
			if(state1.get(expr) != state2.get(expr)) {
				System.out.println("the value for some expression differs: "+ state1.get(expr) + " vs " + state2.get(expr));
				return false;
			}
		}
		return true;
	}

	public void undo() {
		//checkpoint();
		System.out.println("undoing...");
		if(historyPosition>0) {
			System.out.println("really undoing!");
			restore(history.get(historyPosition-1));
			historyPosition--;
			System.out.println("history position: " + historyPosition);
		}
	}

	public void redo() {
		if(historyPosition<history.size()-1) {
			restore(history.get(historyPosition+1));
			historyPosition++;
		}
	}
	
	private final ModifiableExpression<Boolean> changedStatus = Variable.create(false);
	
	public Expression<Boolean> changedStatus() {
		return changedStatus;
	}
}
