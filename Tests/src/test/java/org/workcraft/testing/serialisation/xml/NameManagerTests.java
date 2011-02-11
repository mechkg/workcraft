package org.workcraft.testing.serialisation.xml;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;
import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.SignalTransition;

public class NameManagerTests {
	@Test
	public void test1() {
		STG stg = new STG(new HistoryPreservingStorageManager());

		STGPlace p1 = stg.createPlace();
		STGPlace p2 = stg.createPlace();
		
		Assert.assertNotNull(eval(stg.name(p1)));
		Assert.assertNotNull(eval(stg.name(p2)));
		Assert.assertFalse(eval(stg.name(p2)).equals(eval(stg.name(p1))));

		SignalTransition t1 = stg.createSignalTransition();
		SignalTransition t2 = stg.createSignalTransition();

		ReferenceManager refMan = eval(stg.referenceManager());
		Assert.assertNotNull(refMan.getNodeReference(t1));
		Assert.assertNotNull(refMan.getNodeReference(t2));
		Assert.assertFalse(refMan.getNodeReference(t2).equals(refMan.getNodeReference(t1)));
		
		Assert.assertNotNull(refMan.getNodeReference(stg.getRoot()));
	}
}
