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

package org.workcraft.plugins.stg;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.plugins.petri.Transition;

@VisualClass(VisualSignalTransition.class)
public class SignalTransition extends Transition implements StgTransition 
{

	public SignalTransition(StorageManager storage) {
		super(storage);
		type = storage.create(Type.INTERNAL);
	}
	
	private final ModifiableExpression<Type> type;

	public ModifiableExpression<Type> signalType() {
		return type;
	}

	@Override
	public DummyTransition asDummy() {
		return null;
	}

	@Override
	public SignalTransition asSignal() {
		return this;
	}

	@Override
	public Transition getTransition() {
		return this;
	}
}