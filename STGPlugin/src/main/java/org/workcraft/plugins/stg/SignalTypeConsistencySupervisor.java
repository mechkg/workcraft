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

/**
 * 
 */
package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.workcraft.plugins.stg.SignalTransition.Type;

class SignalTypeConsistencySupervisor {
	
	public SignalTypeConsistencySupervisor(STG stg) {
		this.stg = stg;
	}

	private final STG stg;
	
	public void signalTypeChanged(SignalTransition transition, Type oldType, Type newType) {
		Collection<SignalTransition> sameName = getSameNameTransitions(transition);
		
		if(oldType == null || !oldType.equals(newType)) {
			oldType = newType;
			for (SignalTransition tt : sameName)
			{
				if(tt == transition)
					throw new RuntimeException("qweqwe");
				if (!newType.equals(eval(tt.signalType())))
					tt.signalType().setValue(newType);
			}
		}
	}

	public void nameChanged(SignalTransition transition, String oldName, String newName) {
		Collection<SignalTransition> sameName = getSameNameTransitions(transition);
		Iterator<SignalTransition> iter = sameName.iterator();
		if(iter.hasNext()) {
			System.out.println(String.format("name changed: from %s to %s", oldName, newName));
			if(oldName == null || !oldName.equals(newName)) {
				Type othersSignalType = eval(iter.next().signalType());
				stg.signalType(transition).setValue(othersSignalType);
				
				System.out.println("changing signal type to : " + othersSignalType);
				
			}
		}
	}

	private Collection<SignalTransition> getSameNameTransitions(SignalTransition transition) {
		Collection<SignalTransition> sameName = new ArrayList<SignalTransition>(stg.getSignalTransitions(eval(stg.signalName(transition))));
		sameName.remove(transition);
		return sameName;
	}
}
