package tests.advanced;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class AutoRefreshTest {
	@Test
	public void test1() {
		final Variable<Integer> v = new Variable<Integer>(8);
		final int [] refreshCount = new int[1];
		ExpressionBase<Object> o = new ExpressionBase<Object>() {
			@Override
			protected Object evaluate(EvaluationContext context) {
				refreshCount[0] ++;
				context.resolve(v);
				return null;
			}
		};
		@SuppressWarnings("unused") // used to keep reference to avoid garbage collection
		AutoRefreshExpression refresher = GlobalCache.autoRefresh(o);
		v.setValue(15);
		v.setValue(14);
		v.setValue(13);
		v.setValue(12);
		v.setValue(11);
		v.setValue(10);
		Assert.assertEquals(7, refreshCount[0]);
	}
	@Test
	/**
	 * Tests that there are no glitches on autorefresh -- no expression should ever see an inconsistent state.
	 */
	public void test2() {
		final Variable<Integer> v = new Variable<Integer>(1);
		final boolean [] bad = new boolean[1];
		final int [] res = new int[1];
		final Expression<Integer> a = new Identity(v);
		final Expression<Integer> b = new Identity(v);
		Expression<Integer> o = new ExpressionBase<Integer>() {

			@Override
			protected Integer evaluate(EvaluationContext context) {
				int av = context.resolve(a);
				int bv = context.resolve(b);
				if(av != bv) bad[0] = true;
				res[0] = av+bv;
				return av+bv;
			}
		};
		@SuppressWarnings("unused")
		AutoRefreshExpression refresher = GlobalCache.autoRefresh(o);
		v.setValue(2);
		Assert.assertFalse(bad[0]);
		Assert.assertEquals(4, res[0]);
	}
	
	@Test
	public void test3() {
		final Variable<Integer> v = new Variable<Integer>(1);
		final List<Integer> vs = new ArrayList<Integer>();
		Expression<Integer> o = new ExpressionBase<Integer>() {

			@Override
			protected Integer evaluate(EvaluationContext context) {
				int vv = context.resolve(v);
				vs.add(vv);
				return vv;
			}
		};
		Assert.assertEquals(new ArrayList<Integer>(), vs);
		@SuppressWarnings("unused")
		AutoRefreshExpression refresher = GlobalCache.autoRefresh(o);
		Assert.assertEquals(Arrays.asList(new Integer[]{1}), vs);
		v.setValue(8);
		Assert.assertEquals(Arrays.asList(new Integer[]{1,8}), vs);
	}
}
