package org.workcraft.testing.plugins.stg;

import static java.util.Arrays.asList;
import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STGReferenceManager;
import org.workcraft.plugins.stg.SignalTransition;

public class STGReferenceManagerTests {

	@Test
	public void testGenerateSignalName() {
		SignalTransition transition = new SignalTransition(new DefaultStorageManager());
		STGReferenceManager refMan = new STGReferenceManager();
		refMan.handleEvent(asList(transition), asList(new Node[]{}));
		Assert.assertEquals("signal0", refMan.getState().getInstance(transition).getFirst().getFirst());
	}
	@Test
	public void testGenerateSignalNameTwice() {
		SignalTransition transition1 = new SignalTransition(new DefaultStorageManager());
		SignalTransition transition2 = new SignalTransition(new DefaultStorageManager());
		STGReferenceManager refMan = new STGReferenceManager();
		refMan.handleEvent(asList(transition1), asList(new Node[]{}));
		refMan.handleEvent(asList(transition2), asList(new Node[]{}));
		Assert.assertEquals("signal0", refMan.getState().getInstance(transition1).getFirst().getFirst());
		Assert.assertEquals("signal1", refMan.getState().getInstance(transition2).getFirst().getFirst());
	}
}
