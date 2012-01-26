package tests.advanced;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.DummyEvaluationContext;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.SumExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dependencymanager.util.listeners.Listener;

import static tests.advanced.MemoryTools.*;

public class MemoryExhaustionTest {
	@Test
	public void testTT() {
	    final int M = 1000;
		memoryExhaustionTest(500, new Listener(){
			public void changed(){
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
		memoryExhaustionTest(500, new Listener(){
			public void changed(){
				int totalSum = 0;
				for(int j=0;j<M;j++)
				{
					SumExpression sum = new SumExpression(new Variable<Integer>(8), new Variable<Integer>(9));
					totalSum+=sum.evaluate(new DummyEvaluationContext());
					totalSum+=eval(sum);
				}
				System.out.println ("Total sum: " + totalSum);
			}
		});
	}
	
	@Test
	public void test2() {
	    final int M = 1000;
		memoryExhaustionTest(500, new Listener(){
			public void changed(){
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
