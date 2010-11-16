package tests.advanced;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.Variable;

public final class IndirectIdentity extends ExpressionBase<Integer> {
	private final Variable<Integer> var;

	IndirectIdentity(Variable<Integer> var) {
		this.var = var;
	}

	@Override
	public Integer evaluate(EvaluationContext resolver) {
		return resolver.resolve(new Identity(var));
	}
}