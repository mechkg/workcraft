package tests.advanced;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class DoubleDependencyTest {
	@Test
	public void test() {
		final Variable<Integer> var = new Variable<Integer>(8);
		ExpressionBase<Integer> expression = new ExpressionBase<Integer>() {
			
			@Override
			public Integer evaluate(EvaluationContext resolver) {
				return resolver.resolve(var) + resolver.resolve(var) + resolver.resolve(var) + resolver.resolve(var);
			}
		};
		
		Assert.assertEquals((Integer)32, GlobalCache.eval(expression));
		var.setValue(100);
		Assert.assertEquals((Integer)400, GlobalCache.eval(expression));
	}
}
