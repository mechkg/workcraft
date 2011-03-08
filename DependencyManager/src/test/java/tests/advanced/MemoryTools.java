package tests.advanced;

import org.junit.Assert;
import org.workcraft.dependencymanager.util.listeners.Listener;

public class MemoryTools {
	private static long usedMemory() {
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
	public static void memoryExhaustionTest(int N, Listener job) {
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

	private static void goodGC() {
		System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	}

	private static void fairGC() {
		System.gc(); try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		} System.gc();
	}
}
