/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.testing.serialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.util.Collection;
import java.util.Iterator;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SerialisationTestingUtils {
	public static void comparePlaces (Place p1, Place p2) {
		assertEquals(eval(p1.tokens()), eval(p2.tokens()));
		//assertEquals(p1.getCapacity(), p2.getCapacity());
	}
	
	public static void compareTransitions (STG stg1, SignalTransition t1, STG stg2, SignalTransition t2) {
		assertEquals(eval(stg1.signalName(t1)), eval(stg2.signalName(t2)));
		assertEquals(eval(stg1.direction(t1)), eval(stg2.direction(t2)));
	}
	
	public static void compareConnections (Model model1, MathConnection con1, Model model2, MathConnection con2) {
		compareNodes (model1, con1.getFirst(), model2, con2.getFirst());
		compareNodes (model1, con1.getSecond(), model2, con2.getSecond());
	}
	
	public static void comparePreAndPostSets(VisualComponent c1, VisualComponent c2) {
	/*	assertEquals(c1.getPreset().size(),c2.getPreset().size());
		assertEquals(c1.getPostset().size(),c2.getPostset().size());
		
		Iterator<VisualComponent> i1 = c1.getPreset().iterator();
		Iterator<VisualComponent> i2 = c2.getPreset().iterator();
		
		while ( i1.hasNext() ) {
			VisualComponent n1 = i1.next();
			VisualComponent n2 = i2.next();
			
			assertEquals (n1.getClass(), n2.getClass());
		}
		
		i1 = c1.getPostset().iterator();
		i2 = c2.getPostset().iterator();
		
		while ( i1.hasNext() ) {
			VisualComponent n1 = i1.next();
			VisualComponent n2 = i2.next();
			
			assertEquals (n1.getClass(), n2.getClass());
		}*/
	}
	
	public static void comparePreAndPostSets( MathNode c1, MathNode c2) {
		/*assertEquals(c1.getPreset().size(),c2.getPreset().size());
		assertEquals(c1.getPostset().size(),c2.getPostset().size());
		
		Iterator<MathComponent> i1 = c1.getPreset().iterator();
		Iterator<MathComponent> i2 = c2.getPreset().iterator();
		
		while ( i1.hasNext() ) {
			Component n1 = i1.next();
			Component n2 = i2.next();
			
			assertEquals (n1.getClass(), n2.getClass());
		}
		
		i1 = c1.getPostset().iterator();
		i2 = c2.getPostset().iterator();
		
		while ( i1.hasNext() ) {
			Component n1 = i1.next();
			Component n2 = i2.next();
			
			assertEquals (n1.getClass(), n2.getClass());
		}*/
	}
	
	public static void compareVisualPlaces (VisualPlace p1, VisualPlace p2) {
		//assertEquals(p1.getID(), p2.getID());
		assertEquals(eval(p1.transform()), eval(p2.transform()));
		
		comparePlaces (p1.getReferencedPlace(), p2.getReferencedPlace());
	}
	
	public static void compareVisualSignalTransitions (VisualSTG stg1, VisualSignalTransition t1, VisualSTG stg2, VisualSignalTransition t2) {
		//assertEquals(t1.getID(), t2.getID());
		assertEquals(eval(t1.transform()), eval(t2.transform()));
		
		compareTransitions (stg1.stg, t1.getReferencedTransition(), stg2.stg, t2.getReferencedTransition());
	}
	
	public static void compareVisualConnections (VisualSTG stg1, VisualConnection vc1, VisualSTG stg2, VisualConnection vc2) {
		compareNodes (stg1, vc1.getFirst(), stg2, vc2.getFirst());
		compareNodes (stg1, vc1.getSecond(), stg2, vc2.getSecond());
		
		compareConnections (stg1.stg, vc1.getReferencedConnection(), stg2.stg, vc2.getReferencedConnection());
	}
	
	public static void compareImplicitPlaceArcs (VisualSTG stg1, VisualImplicitPlaceArc vc1, VisualSTG stg2, VisualImplicitPlaceArc vc2) {
		compareNodes (stg1, vc1.getFirst(), stg2, vc2.getFirst());
		compareNodes (stg1, vc1.getSecond(), stg2, vc2.getSecond());
		comparePlaces (vc1.getImplicitPlace(), vc2.getImplicitPlace());
		compareConnections (stg1.stg, vc1.getRefCon1(), stg2.stg, vc2.getRefCon1());
		compareConnections (stg1.stg, vc1.getRefCon2(), stg2.stg, vc2.getRefCon2());
	}
	
	public static void comparePolylines(Polyline p1, Polyline p2) {
		assertEquals(eval(size(p1.children())), eval(size(p2.children())));
		
		Iterator<Node> i1 = eval(p1.children()).iterator();
		Iterator<Node> i2 = eval(p2.children()).iterator();
		
		for (int i=0; i<eval(p1.children()).size(); i++)
		{
			ControlPoint cp1 = (ControlPoint)i1.next();
			ControlPoint cp2 = (ControlPoint)i2.next();
			
			assertEquals (eval(cp1.x()), eval(cp2.x()), 0.0001);
			assertEquals (eval(cp1.y()), eval(cp2.y()), 0.0001);
		}
	}
	
	public static void compareNodes (Model model1, Node node1, Model model2, Node node2) {
		assertEquals(node1.getClass(), node2.getClass());
		
		if (node1 instanceof MathNode)
			comparePreAndPostSets( (MathNode) node1, (MathNode) node2 );
		else if (node1 instanceof VisualComponent)
			comparePreAndPostSets( (VisualComponent) node1, (VisualComponent) node2 );
		
		if (node1 instanceof Place)
			comparePlaces ((Place)node1, (Place)node2);
		else if (node1 instanceof MathConnection)
			compareConnections (model1, (MathConnection)node1, model2, (MathConnection)node2 );
		else if (node1 instanceof SignalTransition)
			compareTransitions ((STG)model1, (SignalTransition)node1, (STG)model2, (SignalTransition)node2 );
		else if (node1 instanceof VisualPlace)
			compareVisualPlaces ( (VisualPlace)node1, (VisualPlace)node2 );
		else if (node1 instanceof VisualSignalTransition)
			compareVisualSignalTransitions ((VisualSTG)model1, (VisualSignalTransition)node1, (VisualSTG)model2, (VisualSignalTransition)node2 );
		else if (node1 instanceof VisualImplicitPlaceArc)
			compareImplicitPlaceArcs ((VisualSTG)model1, (VisualImplicitPlaceArc)node1, (VisualSTG)model2, (VisualImplicitPlaceArc)node2 );
		else if (node1 instanceof VisualConnection)
			compareVisualConnections ((VisualSTG)model1, (VisualConnection)node1, (VisualSTG)model2, (VisualConnection)node2 );
		else if (node1 instanceof Polyline)
			comparePolylines((Polyline)node1, (Polyline)node2);
		else if (node1 instanceof MathGroup);
		else if (node1 instanceof VisualGroup);
		else 
			fail("Unexpected class " + node1.getClass().getName());
				
		Collection<? extends Node> ch1 = eval(node1.children());
		Collection<? extends Node> ch2 = eval(node2.children());
		
		assertEquals(ch1.size(), ch2.size());
		
		Iterator<? extends Node> i1 = ch1.iterator();
		Iterator<? extends Node> i2 = ch2.iterator();
		
		while ( i1.hasNext() ) {
			Node n1 = i1.next();
			Node n2 = i2.next();
			
			compareNodes (model1, n1, model2, n2);
		}
	}
	
	//public 
}
