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

package org.workcraft.plugins.circuit;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;


@VisualClass(org.workcraft.plugins.circuit.VisualFunctionContact.class)
public class FunctionContact extends Contact {
	private ModifiableExpression<BooleanFormula> setFunction;
	private ModifiableExpression<BooleanFormula> resetFunction;
	
	public FunctionContact(IoType ioType, String name, StorageManager storage) {
		super(ioType, name, storage);
		init(storage);
	}

	private void init(StorageManager storage) {
		setFunction = storage.create(BooleanOperations.ZERO);
		resetFunction = storage.create(null);
	}
	
	public FunctionContact(StorageManager storage) {
		super(storage);
		init(storage);
	}
	
	public ModifiableExpression<BooleanFormula> setFunction() {
		return setFunction;
	}
	
	public ModifiableExpression<BooleanFormula> resetFunction() {
		return resetFunction;
	}
}
