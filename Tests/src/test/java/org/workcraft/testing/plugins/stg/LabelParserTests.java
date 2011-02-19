package org.workcraft.testing.plugins.stg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Triple;


public class LabelParserTests {

	@Test
	public void testNoInstance() {
		Triple<String, Direction, Integer> result = LabelParser.parseFull("a+");
		
		assertEquals("a", result.getFirst());
		assertEquals(Direction.PLUS, result.getSecond());
		assertEquals(null, result.getThird());
		
	}
	
	@Test
	public void testInstance() {
		Triple<String, Direction, Integer> result = LabelParser.parseFull("a+/4");
		
		assertEquals("a", result.getFirst());
		assertEquals(Direction.PLUS, result.getSecond());
		assertEquals(new Integer(4), result.getThird());
		
	}
	
	public void testWrongFormat1() {
		assertNull(LabelParser.parseFull("x/"));
	}

	public void testWrongFormat2() {
		assertNull(LabelParser.parseFull("x@/3"));
	}
	
	public void testWrongFormat3() {
		assertNull(LabelParser.parseFull("x-/fifty"));
	}
	
}