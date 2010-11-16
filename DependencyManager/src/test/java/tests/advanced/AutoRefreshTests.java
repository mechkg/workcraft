package tests.advanced;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class AutoRefreshTests {
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
		v.setValue(15);
		v.setValue(15);
		v.setValue(15);
		v.setValue(15);
		v.setValue(15);
		Assert.assertEquals(7, refreshCount[0]);
	}
}
