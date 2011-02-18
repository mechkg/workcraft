package org.workcraft.testing.plugins.stg;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.STGReferenceManager;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgRefManState;
import org.workcraft.serialisation.References;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import static org.junit.Assert.*;

public class STGReferenceManagerTests {

	private final class EmptyNodeContext implements NodeContext {
		@Override
		public Set<Node> getPreset(Node node) {
			return Collections.emptySet();
		}

		@Override
		public Set<Node> getPostset(Node node) {
			return Collections.emptySet();
		}

		@Override
		public Set<Connection> getConnections(Node node) {
			return Collections.emptySet();
		}
	}

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
	
	@Test
	public void testExistingRefs() {
		StorageManager storage = new DefaultStorageManager();
		MathGroup root = new MathGroup(storage);
		final STGPlace p1 = new STGPlace(storage);
		final STGPlace p2 = new STGPlace(storage);
		final STGPlace p3 = new STGPlace(storage);
		final STGPlace p4 = new STGPlace(storage);
		root.add(p1);
		root.add(p2);
		root.add(p3);
		root.add(p4);
		STGReferenceManager.create(root, Expressions.constant(new EmptyNodeContext()), new References() {
			
			@Override
			public Object getObject(String reference) {
				return null;
			}
			
			@Override
			public String getReference(Object obj) {
				if(obj == p4) return "p0";
				else if(obj == p3) return "p1"; else
					if(obj == p2) return "p2";
					else
						if(obj == p1) return "p3";
				return null;
			}
		});
		
	}
}
