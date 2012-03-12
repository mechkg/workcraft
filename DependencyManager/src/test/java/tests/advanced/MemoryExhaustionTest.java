package tests.advanced;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;
import static tests.advanced.MemoryTools.memoryExhaustionTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.DummyEvaluationContext;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.SumExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.util.Action;

public class MemoryExhaustionTest {
	@Test
	public void testTT() {
	    final int M = 1000;
		memoryExhaustionTest(500, new Action(){
			public void run(){
				for(int i=0;i<M;i++) {
					final ExpressionBase<Object> expr = new ExpressionBase<Object>() {
						@Override
						public Object evaluate(EvaluationContext resolver) {
							return this;
						}
					};
					GlobalCache.eval(expr);
				}
			}
		});		
	}
	
	@Test
	public void test1() {
	    final int M = 1000;
		memoryExhaustionTest(500, new Action(){
			public void run(){
				int totalSum = 0;
				for(int j=0;j<M;j++)
				{
					SumExpression sum = new SumExpression(new Variable<Integer>(8), new Variable<Integer>(9));
					totalSum+=sum.evaluate(new DummyEvaluationContext());
					totalSum+=eval(sum);
				}
				Assert.assertEquals((8+9)*M*2, totalSum);
			}
		});
	}
	
	@Test
	public void test2() {
	    final int M = 1000;
		memoryExhaustionTest(500, new Action(){
			public void run(){
				List<Variable<Integer>> vars = new ArrayList<Variable<Integer>>(); 
				List<ExpressionBase<Integer>> expressions = new ArrayList<ExpressionBase<Integer>>();
				for(int i=0;i<M;i++) {
					final Variable<Integer> var = new Variable<Integer>(8);
					vars.add(var);
					final ExpressionBase<Integer> expr =  new IndirectIdentity(var);
					GlobalCache.eval(expr);
					expressions.add(expr);
				}
			}
		});
	}
}
