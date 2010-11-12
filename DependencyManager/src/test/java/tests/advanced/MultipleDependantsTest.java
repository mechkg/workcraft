package tests.advanced;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class MultipleDependantsTest {
	@Test
	public void test() {
		Variable<Integer> var = new Variable<Integer>(8);
		Identity expr1 = new Identity(var); 
		Identity expr2 = new Identity(var);
		Assert.assertEquals((Integer)8, GlobalCache.eval(expr1));
		Assert.assertEquals((Integer)8, GlobalCache.eval(expr2));
		var.setValue(15);
		Assert.assertEquals((Integer)15, GlobalCache.eval(expr1));
		Assert.assertEquals((Integer)15, GlobalCache.eval(expr2));
	}
}
