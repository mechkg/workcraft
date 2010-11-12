package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;

public class SumExpression implements Expression<Integer> {
	private final Expression<Integer> a;
	private final Expression<Integer> b;

	public SumExpression(Expression<Integer> a, Expression<Integer> b)
	{
		this.a = a;
		this.b = b;
	}
	
	public Integer evaluate(EvaluationContext resolver)
	{
		return resolver.resolve(a)+resolver.resolve(b);
	}
}
