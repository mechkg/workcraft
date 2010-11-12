package tests.advanced;

import static org.junit.Assert.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.CacheManager;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.SumExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;


public class Tests {
	private final class StupidVar implements Expression<Integer> {
		private Integer val;

		private StupidVar(Integer val) {
			this.val = val;
		}

		@Override
		public Integer evaluate(EvaluationContext resolver) {
			return val;
		}
	}
	
	private final class Identity implements Expression<Integer> {
		private final Expression<Integer> dependency;

		public Identity(final Expression<Integer> dependency) {
			this.dependency = dependency;
		}

		@Override
		public Integer evaluate(final EvaluationContext resolver) {
			return resolver.resolve(dependency);
		}
	}

	@Test
	public void test0()
	{
		CacheManager resolver = new CacheManager();
		StupidVar var = new StupidVar(5);
		assertEquals(5, resolver.eval(var).intValue());
		var.val = 8;
		assertEquals(5, resolver.eval(var).intValue());
		resolver.changed(var);
		assertEquals(8, resolver.eval(var).intValue());
	}

	@Test
	public void test1()
	{
		CacheManager resolver = new CacheManager();
		StupidVar var = new StupidVar(5);
		Identity id = new Identity(var);
		assertEquals(5, resolver.eval(id).intValue());
		var.val = 8;
		assertEquals(5, resolver.eval(var).intValue());
		assertEquals(5, resolver.eval(id).intValue());
		resolver.changed(var);
		assertEquals(8, resolver.eval(var).intValue());
		assertEquals(8, resolver.eval(id).intValue());
	}


	@Test
	public void test2()
	{
		Variable<Integer> var1 = new Variable<Integer>(5);
		Variable<Integer> var2 = new Variable<Integer>(3);
		SumExpression sum = new SumExpression(var1, var2);
		assertEquals((Integer)8, eval(sum));
		var1.setValue(10);
		assertEquals((Integer)13, eval(sum));
		var2.setValue(10);
		assertEquals((Integer)20, eval(sum));
	}
}
