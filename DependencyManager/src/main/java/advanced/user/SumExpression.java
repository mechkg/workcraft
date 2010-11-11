package advanced.user;

import advanced.core.DependencyResolver;
import advanced.core.Expression;

public class SumExpression implements Expression<Integer> {
	private final Expression<Integer> a;
	private final Expression<Integer> b;

	public SumExpression(Expression<Integer> a, Expression<Integer> b)
	{
		this.a = a;
		this.b = b;
	}
	
	public Integer evaluate(DependencyResolver resolver)
	{
		return resolver.resolve(a)+resolver.resolve(b);
	}
}
