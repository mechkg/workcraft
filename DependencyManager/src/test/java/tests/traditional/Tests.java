package tests.traditional;

import static org.junit.Assert.*;

import org.junit.Test;
import org.workcraft.dependencymanager.traditional.user.SimpleVariable;
import org.workcraft.dependencymanager.traditional.user.SumExpression;


public class Tests {
	@Test
	public void test1()
	{
		SimpleVariable<Integer> var1 = new SimpleVariable<Integer>(8);
		SimpleVariable<Integer> var2 = new SimpleVariable<Integer>(15);
		SumExpression sum = new SumExpression(var1, var2);
		assertEquals(23, sum.getValue());
		var1.setValue(10);
		assertEquals(25, sum.getValue());
		var2.setValue(10);
		assertEquals(20, sum.getValue());
	}
}
