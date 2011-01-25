package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class HistoryPreservingStorageManager implements StorageManager {

	HashSet<ModifiableExpression<?>> allVars = new HashSet<ModifiableExpression<?>>();
	private Map<ModifiableExpression<?>, Object> savedState;
	
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

	public void dosave() {
		System.out.println("saving");
		savedState = save();
	}

	public void dorestore() {
		System.out.println("restoring");
		restore(savedState);
	}

}
