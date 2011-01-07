package tests.advanced;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;

public final class Identity extends ExpressionBase<Integer> {
	private final ExpressionBase<Integer> dependency;

	public Identity(final ExpressionBase<Integer> dependency) {
		this.dependency = dependency;
	}

	@Override
	public Integer evaluate(final EvaluationContext resolver) {
		return resolver.resolve(dependency);
	}
}