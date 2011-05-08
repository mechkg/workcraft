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

package org.workcraft.plugins.cpog;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.One;

public class Vertex implements Node, Component
{
	public static Vertex make(StorageManager storage) {
		return new Vertex(storage.<BooleanFormula>create(One.instance()), VisualComponent.make(storage));
	}
	
	public Vertex(ModifiableExpression<BooleanFormula> condition, VisualComponent visualInfo) {
		this.condition = condition;
		this.visualInfo = visualInfo;
	}

	public final ModifiableExpression<BooleanFormula> condition;
	public final VisualComponent visualInfo;
	@Override
	public <T> T accept(NodeVisitor<T> visitor) {
		return visitor.visitComponent(this);
	}
	
	@Override
	public <T> T accept(ComponentVisitor<T> visitor) {
		return visitor.visitVertex(this);
	}
}
