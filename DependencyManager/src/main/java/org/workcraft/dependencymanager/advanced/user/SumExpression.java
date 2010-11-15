package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;

public class SumExpression extends Expression<Integer> {
	private final Expression<Integer> a;
	private final Expression<Integer> b;

	public SumExpression(Expression<Integer> a, Expression<Integer> b)
	{
		this.a = a;
		this.b = b;
	}
	
	@Override
	public Integer evaluate(EvaluationContext context)
	{
		return context.resolve(a)+context.resolve(b);
	}
}
