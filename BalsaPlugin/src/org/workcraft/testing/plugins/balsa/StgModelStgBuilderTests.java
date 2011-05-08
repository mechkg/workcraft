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

package org.workcraft.testing.plugins.balsa;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.HandshakeVisitor;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.NameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
public class StgModelStgBuilderTests {
	
	@Test
	public void Test1()
	{
		STG stg = new STG(new DefaultStorageManager());
		
		Handshake hs1 = new Handshake()
		{
			@Override public String toString(){ return "hs1"; }

			@Override public <T> T accept(HandshakeVisitor<T> v) {
				throw new RuntimeException ("Not implemented");
			}

			@Override public boolean isActive() {
				throw new RuntimeException ("Not implemented");
			}
		};
		
		StgBuilder builder =  new StgModelStgBuilder(stg, new NameProvider<Handshake>(){
			@Override
			public String getName(Handshake handshake) {
				return handshake.toString();
			}
		});
		
		builder.buildSignal(new SignalId(hs1, "s1"), true);
		SignalTransition [] transitions = stg.getTransitions().toArray(new SignalTransition[0]);
		
		findSignal(stg, transitions, "hs1_s1", Type.OUTPUT);
	}

	private void findSignal(STG stg, SignalTransition[] transitions, String signalName, Type type) {

		boolean plusFound = false;
		boolean minusFound = false;
		
		for(SignalTransition t : transitions)
		{
			if(!eval(stg.signalName(t)).equals(signalName))
				continue;
			if(!eval(t.signalType()).equals(type))
				continue;
			if(!eval(stg.direction(t)).equals(Direction.MINUS))
				minusFound = true;
			if(!eval(stg.direction(t)).equals(Direction.PLUS))
				plusFound = true;
		}
		Assert.assertTrue(plusFound && minusFound);
	}
}
