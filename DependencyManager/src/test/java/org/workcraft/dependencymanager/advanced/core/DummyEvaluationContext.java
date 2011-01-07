package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;

public class DummyEvaluationContext implements EvaluationContext {
	public static DummyEvaluationContext INSTANCE = new DummyEvaluationContext();
	@Override
	public <T> T resolve(Expression<T> dependency) {
		return dependency.getValue(null).value;
	}
}
