package org.workcraft.testing.serialisation.xml;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;

public class NameManagerTests {
	@Test
	public void test1() {
		STG stg = new STG(new HistoryPreservingStorageManager());

		Place p1 = stg.createPlace();
		Place p2 = stg.createPlace();
		
		Assert.assertNotNull(stg.getName(p1));
		Assert.assertNotNull(stg.getName(p2));
		Assert.assertFalse(stg.getName(p2).equals(stg.getName(p1)));

		SignalTransition t1 = stg.createSignalTransition();
		SignalTransition t2 = stg.createSignalTransition();

		Assert.assertNotNull(stg.getName(t1));
		Assert.assertNotNull(stg.getName(t2));
		Assert.assertFalse(stg.getName(t2).equals(stg.getName(t1)));
		
		Assert.assertNotNull(stg.getName(stg.getRoot()));
	}
}
