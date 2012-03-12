package tests.advanced;

import static org.junit.Assert.assertEquals;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.SumExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;


public class SimpleTest {
	private final class StupidVar extends ExpressionBase<Integer> {
		private Integer val;

		private StupidVar(Integer val) {
			this.val = val;
		}

		@Override
		public Integer evaluate(EvaluationContext resolver) {
			return val;
		}
	}
	
	@Test
	public void test0()
	{
		StupidVar var = new StupidVar(5);
		assertEquals(5, eval(var).intValue());
		var.val = 8;
		assertEquals(5, eval(var).intValue());
		//resolver.changed(var);
		var.refresh();
		assertEquals(8, eval(var).intValue());
	}

	@Test
	public void test1()
	{
		StupidVar var = new StupidVar(5);
		Identity id = new Identity(var);
		assertEquals(5, eval(id).intValue());
		var.val = 8;
		assertEquals(5, eval(var).intValue());
		assertEquals(5, eval(id).intValue());
		//resolver.changed(var);
		var.refresh();
		assertEquals(8, eval(var).intValue());
		assertEquals(8, eval(id).intValue());
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
