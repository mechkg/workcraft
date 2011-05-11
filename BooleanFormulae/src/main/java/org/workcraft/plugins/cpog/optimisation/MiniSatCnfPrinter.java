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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.workcraft.util.Function;

public class MiniSatCnfPrinter<Var> implements CnfPrinter<Var>
{
	public MiniSatCnfPrinter(Function<Var, String> labelProvider) {
		this.labelProvider = labelProvider;
	}
	
	final Function<Var, String> labelProvider;
	Map<Var, Integer> numbers = new HashMap<Var, Integer>();
	int varCount = 0;
	private Cnf<Var> cnf;
	
	@Override
	public String print(Cnf<Var> cnf) 
	{
		this.cnf = cnf;
		StringBuilder result = new StringBuilder(); 
		for(CnfClause<Var> clause : cnf.getClauses())
		{
			for(Literal<Var> literal : clause.getLiterals())
			{
				Integer number = getNumber(literal.getVariable());
				if(literal.getNegation())
					result.append("-");
				result.append(number.toString());
				result.append(" ");
			}
			result.append("0");
			result.append("\n");
		}
		
		result.insert(0, getHeadLine());
		result.insert(0, getHeadComments());
		
		return result.toString();
	}

	private String getHeadComments() {
		StringBuilder result = new StringBuilder();
		
		for(Var var : new TreeMap<Var, Integer>(numbers).keySet()) {
			String label = labelProvider.apply(var);
			if(!label.isEmpty())
				result.append("c " + numbers.get(var) + " " + label+"\n");
		}
		
		return result.toString();
	}

	private String getHeadLine() {
		return "p cnf " + varCount + " " + cnf.getClauses().size()+"\n";
	}

	private Integer getNumber(Var variable) {
		Integer res = numbers.get(variable);
		if(res == null)
		{
			res = ++varCount;
			numbers.put(variable, res);
		}
		return res;
	}
}
