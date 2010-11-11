package tests.advanced;

import static advanced.core.GlobalCache.*;
import static org.junit.Assert.*;

import org.junit.Test;

import advanced.core.CacheManager;
import advanced.core.DependencyResolver;
import advanced.core.Expression;
import advanced.user.SumExpression;
import advanced.user.Variable;

public class Tests {
	private final class StupidVar implements Expression<Integer> {
		private Integer val;

		private StupidVar(Integer val) {
			this.val = val;
		}

		@Override
		public Integer evaluate(DependencyResolver resolver) {
			return val;
		}
	}
	
	private final class Identity implements Expression<Integer> {
		private final Expression<Integer> dependency;

		public Identity(final Expression<Integer> dependency) {
			this.dependency = dependency;
		}

		@Override
		public Integer evaluate(final DependencyResolver resolver) {
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
