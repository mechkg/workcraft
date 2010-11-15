package org.workcraft.dependencymanager.advanced.core;

public class DummyEvaluationContext implements EvaluationContext {
	public <T> T resolve(Handle<T> dependency) {
		return dependency.expression.evaluate(this);
	}
	public void changed() {
		
	}
	@Override
	public <T> T resolve(IExpression<T> dependency) {
		return dependency.getValue(null).value;
	}
}
