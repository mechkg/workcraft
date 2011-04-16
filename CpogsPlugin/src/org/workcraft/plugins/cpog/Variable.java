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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class Variable implements Comparable<Variable>, BooleanVariable, Node, Component
{
	public final ModifiableExpression<VariableState> state;
	public final ModifiableExpression<String> label;
	public final VisualComponent visualVar;

	public Variable(ModifiableExpression<VariableState> state, ModifiableExpression<String> label, VisualComponent visualVar) {
		this.state = state;
		this.label = label;
		this.visualVar = visualVar;
	}
	
	public static Variable make(StorageManager storage, ModifiableExpression<String> varName) {
		return new Variable
				( storage.create(VariableState.UNDEFINED)
				, varName
				, new VisualComponent(storage.<Point2D>create(new Point2D.Double(0, 0)), varName, storage.create(LabelPositioning.BOTTOM)));
	}
	
	public ModifiableExpression<VariableState> state()
	{
		return state;
	}	

	@Override
	public int compareTo(Variable o)
	{
		return getLabel().compareTo(o.getLabel());
	}
	
	@Override
	public String getLabel() {
		return eval(label);
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <T> T accept(NodeVisitor<T> visitor) {
		return visitor.visitComponent(this);
	}

	@Override
	public <T> T accept(ComponentVisitor<T> visitor) {
		return visitor.visitVariable(this);
	}
}
