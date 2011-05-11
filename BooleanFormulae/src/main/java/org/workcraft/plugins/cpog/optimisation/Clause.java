/*
*
* Copyright 2008,2009,2010 Newcastle University
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
package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class Clause<L> {

	private List<L> literals = new ArrayList<L>();
	
	public Clause()
	{
	}
	
	public Clause(L... literals)
	{
		this(Arrays.asList(literals));
	}

	public Clause(List<L> literals) {
		this.setLiterals(literals);
	}

	public void setLiterals(List<L> literals) {
		this.literals = literals;
	}

	public List<L> getLiterals() {
		return literals;
	}
	
	public void add(List<L> list)
	{
		literals.addAll(list);
	}
	
	public void add(L... arr)
	{
		literals.addAll(Arrays.asList(arr));
	}
}
