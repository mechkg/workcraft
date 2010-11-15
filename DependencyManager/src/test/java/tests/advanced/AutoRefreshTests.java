package tests.advanced;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class AutoRefreshTests {
	@Test
	public void test1() {
		final Variable<Integer> v = new Variable<Integer>(8);
		final int [] refreshCount = new int[1];
		Expression<Object> o = new Expression<Object>() {
			@Override
			protected Object evaluate(EvaluationContext context) {
				refreshCount[0] ++;
				context.resolve(v);
				return null;
			}
		};
		GlobalCache.autoRefresh(o);
		v.setValue(15);
		v.setValue(15);
		v.setValue(15);
		v.setValue(15);
		v.setValue(15);
		v.setValue(15);
		Assert.assertEquals(7, refreshCount[0]);
	}
}
