package org.workcraft.plugins.stg;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotSupportedException;

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
