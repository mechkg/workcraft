package org.workcraft.testing.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;
import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;

public class StgMathNodeRemovalTest {
	@Test
	public void mathNodeRemovalTest() throws InvalidConnectionException {
		DefaultStorageManager storage = new DefaultStorageManager();
		STG stg = new STG(storage);
		VisualSTG visualStg = new VisualSTG(stg, storage);
		VisualDummyTransition transition = visualStg.createDummyTransition();
		VisualPlace place = visualStg.createPlace();
		VisualConnection arc = visualStg.createConnection(place, transition);
		MathNode mathArc = arc.getReferencedConnection();
		Assert.assertTrue(eval(stg.getRoot().children()).contains(mathArc));
		visualStg.remove(arc);
		Assert.assertFalse(eval(stg.getRoot().children()).contains(mathArc));
	}
}
