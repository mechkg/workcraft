package tests.advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.Variable;


public class TooWeakRefsTest {
	@Test
	public void test() {
		int N = 10000;
		//int M = 10;
		List<Variable<Integer>> vars = new ArrayList<Variable<Integer>>(); 
		List<Expression<Integer>> expressions = new ArrayList<Expression<Integer>>();
		for(int i=0;i<N;i++) {
			final Variable<Integer> var = new Variable<Integer>(8);
			vars.add(var);
			final Expression<Integer> expr =  new IndirectIdentity(var);
			GlobalCache.eval(expr);
			expressions.add(expr);
		}
		Runtime.getRuntime().gc();
		Random r = new Random(0);
		for(int i=0;i<N;i++)
		{
			Integer val = r.nextInt();
			vars.get(i).setValue(val);
			Assert.assertEquals(val, GlobalCache.eval(expressions.get(i)));
		}
		
	}
}
