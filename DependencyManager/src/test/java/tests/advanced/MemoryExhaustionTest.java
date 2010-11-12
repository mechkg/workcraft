package tests.advanced;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.DummyEvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.SumExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dependencymanager.util.listeners.Listener;


public class MemoryExhaustionTest {
	public void test(int N, Listener job) {
		
		goodGC();
	    long startMem = usedMemory();
	    
	    int memoryLimitMb = 15;
	    
	    for(int i=0;i<N;i++)
		{
			job.changed();
			fairGC();
			long additionalMemoryUsed = usedMemory()-startMem;
			System.out.println(String.format("working (%s), additional memory used: %s...", i, additionalMemoryUsed));
			String message = String.format("additional memory usage should be < %sMb, actual memory usage is %s after %s iterations", memoryLimitMb, additionalMemoryUsed, i);
			Assert.assertTrue(message, additionalMemoryUsed<memoryLimitMb*1000000);
		}
	}

	private void goodGC() {
		System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	}

	private void fairGC() {
		System.gc(); try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		} System.gc();
	}

	@Test
	public void test1() {
	    final int M = 1000;
		test(500, new Listener(){
			public void changed(){
				int totalSum = 0;
				for(int j=0;j<M;j++)
				{
					SumExpression sum = new SumExpression(new Variable<Integer>(8), new Variable<Integer>(9));
					totalSum+=sum.evaluate(new DummyEvaluationContext());
					totalSum+=eval(sum);
				}
			}
		});
	}
	
	@Test
	public void test2() {
	    final int M = 1000;
		test(500, new Listener(){
			public void changed(){
				List<Variable<Integer>> vars = new ArrayList<Variable<Integer>>(); 
				List<Expression<Integer>> expressions = new ArrayList<Expression<Integer>>();
				for(int i=0;i<M;i++) {
					final Variable<Integer> var = new Variable<Integer>(8);
					vars.add(var);
					final Expression<Integer> expr =  new IndirectIdentity(var);
					GlobalCache.eval(expr);
					expressions.add(expr);
				}
			}
		});
	}
	
	private long usedMemory() {
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
}
