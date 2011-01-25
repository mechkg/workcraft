package org.workcraft.testing.plugins.stg;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STGReferenceManager;
import org.workcraft.plugins.stg.SignalTransition;

public class STGReferenceManagerTests {

	@Test
	public void testGenerateSignalName() {
		SignalTransition transition = new SignalTransition(new DefaultStorageManager());
		STGReferenceManager refMan = new STGReferenceManager(new MathNode(new DefaultStorageManager()){}, null);
		refMan.handleEvent(Arrays.asList(new Node[]{transition}), Arrays.asList(new Node[]{}));
		Assert.assertEquals("signal0", GlobalCache.eval(transition.signalName()));
	}
	@Test
	public void testGenerateSignalNameFromNull() {
		SignalTransition transition = new SignalTransition(new DefaultStorageManager());
		transition.signalName().setValue(null);
		STGReferenceManager refMan = new STGReferenceManager(new MathGroup(new DefaultStorageManager()), null);
		refMan.handleEvent(Arrays.asList(new Node[]{transition}), Arrays.asList(new Node[]{}));
		Assert.assertEquals("signal0", GlobalCache.eval(transition.signalName()));
	}
	@Test
	public void testGenerateSignalNameFromEmpty() {
		SignalTransition transition = new SignalTransition(new DefaultStorageManager());
		transition.signalName().setValue("");
		STGReferenceManager refMan = new STGReferenceManager(new MathNode(new DefaultStorageManager()){}, null);
		refMan.handleEvent(Arrays.asList(new Node[]{transition}), Arrays.asList(new Node[]{}));
		Assert.assertEquals("signal0", GlobalCache.eval(transition.signalName()));
	}
	@Test
	public void testGenerateSignalNameTwice() {
		SignalTransition transition1 = new SignalTransition(new DefaultStorageManager());
		SignalTransition transition2 = new SignalTransition(new DefaultStorageManager());
		STGReferenceManager refMan = new STGReferenceManager(new MathGroup(new DefaultStorageManager()), null);
		refMan.handleEvent(Arrays.asList(new Node[]{transition1}), Arrays.asList(new Node[]{}));
		refMan.handleEvent(Arrays.asList(new Node[]{transition2}), Arrays.asList(new Node[]{}));
		Assert.assertEquals("signal0", GlobalCache.eval(transition1.signalName()));
		Assert.assertEquals("signal1", GlobalCache.eval(transition2.signalName()));
	}
}
