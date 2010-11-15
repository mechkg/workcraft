package tests.advanced;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;

public final class Identity extends Expression<Integer> {
	private final Expression<Integer> dependency;

	public Identity(final Expression<Integer> dependency) {
		this.dependency = dependency;
	}

	@Override
	public Integer evaluate(final EvaluationContext resolver) {
		return resolver.resolve(dependency);
	}
}