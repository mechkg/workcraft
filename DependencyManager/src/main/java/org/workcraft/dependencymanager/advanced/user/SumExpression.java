package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;

public class SumExpression extends ExpressionBase<Integer> {
	private final ExpressionBase<Integer> a;
	private final ExpressionBase<Integer> b;

	public SumExpression(ExpressionBase<Integer> a, ExpressionBase<Integer> b)
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
