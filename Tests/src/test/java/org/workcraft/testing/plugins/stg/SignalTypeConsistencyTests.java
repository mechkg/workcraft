package org.workcraft.testing.plugins.stg;

import org.junit.Test;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;

import static org.junit.Assert.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class SignalTypeConsistencyTests {
	@Test
	public void testTypeNameChange() {
		STG stg = new STG(new DefaultStorageManager());
		SignalTransition t1 = stg.createSignalTransition("signal1", Direction.PLUS);
		SignalTransition t2 = stg.createSignalTransition("signal1", Direction.PLUS);
		stg.signalType(t2).setValue(Type.INPUT);
		assertEquals(Type.INPUT, eval(stg.signalType(t1)));
	}
	@Test
	public void testSignalNameChange() {
		STG stg = new STG(new DefaultStorageManager());
		SignalTransition t1 = stg.createSignalTransition("signal1", Direction.PLUS);
		SignalTransition t2 = stg.createSignalTransition("signal2", Direction.PLUS);
		stg.signalType(t2).setValue(Type.INPUT);
		stg.signalName(t1).setValue("signal2");
		assertEquals(Type.INPUT, eval(stg.signalType(t1)));
	}
	@Test
	public void testNameChange() {
		STG stg = new STG(new DefaultStorageManager());
		SignalTransition t1 = stg.createSignalTransition("signal1", Direction.PLUS);
		SignalTransition t2 = stg.createSignalTransition("signal2", Direction.PLUS);
		stg.signalType(t2).setValue(Type.INPUT);
		stg.setName(t1, "signal2+");
		assertEquals(Type.INPUT, eval(stg.signalType(t1)));
	}
	@Test
	public void testNameChangeNoSimilarNames() {
		STG stg = new STG(new DefaultStorageManager());
		SignalTransition t1 = stg.createSignalTransition("signal1", Direction.PLUS);
		stg.setName(t1, "signal2+");
	}
}
