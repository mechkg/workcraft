package org.workcraft.testing.plugins.stg;

import static java.util.Arrays.asList;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.STGReferenceManager;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgRefManState;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import static org.junit.Assert.*;

public class STGReferenceManagerTests {

	@Test
	public void testGenerateSignalName() {
		SignalTransition transition = new SignalTransition(new DefaultStorageManager());
		STGReferenceManager refMan = new STGReferenceManager();
		refMan.handleEvent(asList(transition), asList(new Node[]{}));
		assertEquals("signal0", refMan.getState().getInstance(transition).getFirst().getFirst());
	}
	@Test
	public void testGenerateSignalNameTwice() {
		SignalTransition transition1 = new SignalTransition(new DefaultStorageManager());
		SignalTransition transition2 = new SignalTransition(new DefaultStorageManager());
		STGReferenceManager refMan = new STGReferenceManager();
		refMan.handleEvent(asList(transition1), asList(new Node[]{}));
		refMan.handleEvent(asList(transition2), asList(new Node[]{}));
		assertEquals("signal0", refMan.getState().getInstance(transition1).getFirst().getFirst());
		assertEquals("signal1", refMan.getState().getInstance(transition2).getFirst().getFirst());
	}
	
	@Test
	public void testUpdatesExpressionValue() {
		final STGPlace place = new STGPlace(new DefaultStorageManager());
		STGReferenceManager refMan = new STGReferenceManager();
		refMan.setName(place, "a_place");
		refMan.startHierarchySupervision(new MathGroup(new DefaultStorageManager()));
		final Expression<StgRefManState> state = refMan.state();
		Expression<String> placeName = new ExpressionBase<String>(){

			@Override
			protected String evaluate(EvaluationContext context) {
				return context.resolve(state).getName(place);
			}
		};
		assertEquals("a_place", eval(placeName));
		refMan.setName(place, "another_name");
		assertEquals("another_name", eval(placeName));
		refMan.setName(place, "another_name_yet");
		assertEquals("another_name_yet", eval(placeName));
	}
}
