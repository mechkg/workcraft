package tests.advanced;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.DummyEvaluationContext;
import org.workcraft.dependencymanager.advanced.user.SumExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;


public class MemoryExhaustionTest {
	@Test
	public void test() {
		
		Runtime.getRuntime().gc();
	    long startMem = usedMemory();
	    int totalSum = 0;
	    
	    int N = 1000;
	    int M = 1000;
	    int memoryLimitMb = 40;
	    
	    for(int i=0;i<N;i++)
		{
			for(int j=0;j<M;j++)
			{
				SumExpression sum = new SumExpression(new Variable<Integer>(8), new Variable<Integer>(9));
				totalSum+=sum.evaluate(new DummyEvaluationContext());
				totalSum+=eval(sum);
			}
			Runtime.getRuntime().gc();
			long additionalMemoryUsed = usedMemory()-startMem;
			System.out.println(String.format("working (%s), additional memory used: %s...", i, additionalMemoryUsed));
			String message = String.format("additional memory usage should be < %sMb, actual memory usage is %s after %s iterations", memoryLimitMb, additionalMemoryUsed, i*M);
			Assert.assertTrue(message, additionalMemoryUsed<memoryLimitMb*1000000);
		}
	}

	private long usedMemory() {
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
}
