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

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.util.Func;
import org.workcraft.util.Null;


//TODO: supervise signal consistency in some other way, to get rid of the listener
class SignalTypeConsistencySupervisor extends StateSupervisor {
	static class SupervisionNode extends ExpressionBase<Null> {

		private final SignalTransition transition;
		private String oldName = null;
		private SignalTransition.Type oldType = null;
		private final STG stg;

		public SupervisionNode(STG stg, SignalTransition transition) {
			this.stg = stg;
			this.transition = transition;
		}
		
		@Override
		public Null evaluate(EvaluationContext context) {

			String signalName = context.resolve(transition.signalName());
			
			Collection<SignalTransition> sameName = new ArrayList<SignalTransition>(stg.getSignalTransitions(signalName));
			sameName.remove(transition);
			
			System.out.println("updated for node " + transition);
			
			if(oldName == null || !oldName.equals(signalName)) {
				if (!sameName.isEmpty())
				{
					// look out! replacing 'eval' with 'context.resolve' will make this expression self-modifying  
					transition.signalType().setValue(eval(sameName.iterator().next().signalType()));
				}
				// subscribe to transition signal type changes:
				oldType = context.resolve(transition.signalType()); 
				
			} else {
				SignalTransition.Type signalType = context.resolve(transition.signalType());
				if(oldType == null || !oldType.equals(signalType)) {
					oldType = signalType;
					for (SignalTransition tt : sameName)
					{
						if(tt == transition)
							throw new RuntimeException("qweqwe");
						if (!signalType.equals(eval(tt.signalType())))
							tt.signalType().setValue(signalType);
					}
				}
			}
			
			oldName = signalName;

			return null;
		}
		
	}
	
	static class SupervisionFunc implements Func<Node, SupervisionNode> {
		private final STG stg;

		public SupervisionFunc(STG stg) {
			this.stg = stg;
		}

		@Override
		public SupervisionNode eval(Node node) {
			if(node instanceof SignalTransition)
				return new SupervisionNode(stg, (SignalTransition)node);
			else
				return null;
		}
	}
	
	SignalTypeConsistencySupervisor(STG stg) {
		super(stg.getRoot(), new SupervisionFunc(stg));
	}
}
