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

package org.workcraft.testing.dom;

import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.DefaultStorageManager;

import static org.junit.Assert.*;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class DOMTests {
	
	@Test
	public void Test1 () throws InvalidConnectionException {
		PetriNet pn = new PetriNet(new DefaultStorageManager());
		
		Place p1 = pn.createPlace();
		Place p2 = pn.createPlace();
		
		Transition t1 = pn.createTransition();
		
		Connection con1 = pn.connect(p1, t1);
		Connection con2 = pn.connect(t1, p2);
		
		assertSame (p1, eval(pn.referenceManager()).getNodeByReference(eval(pn.referenceManager()).getNodeReference(p1)));
		assertSame (p2, eval(pn.referenceManager()).getNodeByReference(eval(pn.referenceManager()).getNodeReference(p2)));
		
		
		assertTrue (eval(pn.nodeContext()).getPreset(p2).contains(t1));
		assertTrue (eval(pn.nodeContext()).getPostset(p1).contains(t1));
		
		assertTrue (eval(pn.nodeContext()).getConnections(p1).contains(con1));
		
		pn.remove(p1);
		
		assertTrue (eval(pn.nodeContext()).getConnections(t1).contains(con2));
		assertFalse (eval(pn.nodeContext()).getConnections(t1).contains(con1));
		
		boolean thrown = true; 
		try
		{
			eval(pn.referenceManager()).getNodeReference(p1);
			thrown = false;
		}catch(Throwable th) {}
		
		assertTrue(thrown);
	}

}
