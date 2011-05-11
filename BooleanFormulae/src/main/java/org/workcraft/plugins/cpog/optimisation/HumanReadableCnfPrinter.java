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
package org.workcraft.plugins.cpog.optimisation;

import org.workcraft.util.Function;

public class HumanReadableCnfPrinter<Var> implements CnfPrinter<Var> {

	final Function<Var, String> varPrinter;
	
	public HumanReadableCnfPrinter(Function<Var, String> varPrinter) {
		super();
		this.varPrinter = varPrinter;
	}

	@Override
	public String print(Cnf<Var> cnf) 
	{
		StringBuilder result = new StringBuilder(); 
		for(CnfClause<Var> clause : cnf.getClauses())
		{
			for(Literal<Var> literal : clause.getLiterals())
			{
				result.append(varPrinter.apply(literal.getVariable()));
				if(literal.getNegation())
					result.append("'");
				result.append(" ");
			}
			result.append("\n");
		}
		return result.toString();
	}
}
