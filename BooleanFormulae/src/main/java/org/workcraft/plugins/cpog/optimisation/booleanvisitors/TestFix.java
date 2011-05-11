package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import org.junit.Test;
import org.workcraft.util.Function;

public class TestFix {
	
	Integer factorialStep(Function<Integer, Integer> rec, Integer n) {
		if(n == 0)
			return 1;
		else
			return n * rec.apply(n-1);
	}
	
	@Test
	public void testFix() {
		Function<Function<Integer, Integer>, Function<Integer, Integer>> factorialStep = new Function<Function<Integer, Integer>, Function<Integer, Integer>>() {
			@Override
			public Function<Integer, Integer> apply(final Function<Integer, Integer> rec) {
				return new Function<Integer, Integer>(){
					@Override
					public Integer apply(Integer argument) {
						return factorialStep(rec, argument);
					}
				};
			}
		};
		
		Function<Integer, Integer> factorial = Fix.fix(factorialStep);
		
		System.out.println(factorial.apply(8));
	}
}
