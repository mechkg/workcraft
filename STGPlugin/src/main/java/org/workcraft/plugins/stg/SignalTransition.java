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

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class SignalTransition extends Transition implements StgTransition 
{
	public enum Type {
		INPUT,
		OUTPUT,
		INTERNAL,
		DUMMY
	}

	public enum Direction {
		PLUS,
		MINUS,
		TOGGLE;
		
		public static Direction fromString(String s) {
			if (s.equals("+"))
				return PLUS;
			else if (s.equals("-"))
				return MINUS;
			else if (s.equals("~"))
				return TOGGLE;
			
			throw new ArgumentException ("Unexpected string: " + s);
		}
	
	
		@Override public String toString() {
			switch(this)
			{
			case PLUS:
				return "+";
			case MINUS:
				return "-";
			case TOGGLE:
				return "~"; 
			default:
				throw new NotSupportedException();
			}
		}
	}

	public SignalTransition(StorageManager storage) {
		super(storage);
		type = storage.create(Type.INTERNAL);
		direction = storage.create(Direction.TOGGLE);
		signalName = storage.create(null);
	}
	
	private final ModifiableExpression<Type> type;
	private final ModifiableExpression<Direction> direction;
	private final ModifiableExpression<String> signalName;

	public ModifiableExpression<Type> signalType() {
		return type;
	}

	public ModifiableExpression<Direction> direction() {
		return direction;
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<String> signalName() {
		return signalName;
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