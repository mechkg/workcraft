package org.workcraft.dependencymanager.advanced.core;

public class DummyEvaluationContext implements EvaluationContext {
	public <T> T resolve(Expression<T> dependency) {
		return dependency.evaluate(this);
	}
	public void changed() {
		
	}
}
