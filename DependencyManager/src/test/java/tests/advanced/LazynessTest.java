package tests.advanced;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.SumExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class LazynessTest {
	
	static class LazyVar extends Variable<Integer> {

		public int evals = 0;
		public LazyVar(Integer value) {
			super(value);
		}
		
		@Override
		protected Integer evaluate(EvaluationContext context) {
			evals ++;
			return super.evaluate(context);
		}
	}
	
	static class LazySum extends SumExpression {

		public int evals = 0;
		
		public LazySum(ExpressionBase<Integer> a, ExpressionBase<Integer> b) {
			super(a, b);
		}
		
		@Override
		public Integer evaluate(EvaluationContext context) {
			evals++;
			return super.evaluate(context);
		}
	}
	
	static LazySum sum(ExpressionBase<Integer> a, ExpressionBase<Integer> b) {
		return new LazySum(a,b);
	}
	
	@Test
	public void test1() {
		LazyVar a = new LazyVar(8);
		LazyVar b = new LazyVar(8);
		LazyVar c = new LazyVar(8);
		LazyVar d = new LazyVar(8);
		LazyVar e = new LazyVar(8);
		LazyVar f = new LazyVar(8);
		LazySum sa = sum(a, b);
		LazySum sb = sum(b, c);
		LazySum sc = sum(d, e);
		LazySum sd = sum(e, f);
		LazySum ssa = sum(sa, sb);
		LazySum ssb = sum(sc, sd);
		LazySum sss = sum(ssa, ssb);
		Assert.assertEquals(64, GlobalCache.eval(sss).intValue());
		a.setValue(10);
		Assert.assertEquals(66, GlobalCache.eval(sss).intValue());
		Assert.assertEquals(2, a.evals);
		Assert.assertEquals(1, b.evals);
		Assert.assertEquals(2, sa.evals);
		Assert.assertEquals(1, sb.evals);
		b.setValue(10);
		Assert.assertEquals(70, GlobalCache.eval(sss).intValue());
		Assert.assertEquals(2, a.evals);
		Assert.assertEquals(2, b.evals);
		Assert.assertEquals(3, sa.evals);
		Assert.assertEquals(2, sb.evals);
		Assert.assertEquals(1, e.evals);
		Assert.assertEquals(1, f.evals);
		Assert.assertEquals(1, ssb.evals);
	}
}
