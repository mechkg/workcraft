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

import org.workcraft.util.Function;

public class SimpleCnfTaskProvider<Var> implements RawCnfGenerator<Var, Cnf<Var>> 
{
	private final Function<Var, String> labelProvider;

	public SimpleCnfTaskProvider(Function<Var, String> labelProvider) {
		this.labelProvider = labelProvider;
	}
	
	@Override
	public CnfTask<Var> getCnf(Cnf<Var> cnf) {
		Map<String, Var> vars = new HashMap<String, Var>();
		
		for(CnfClause<Var> clause : cnf.getClauses())
			for(Literal<Var> literal : clause.getLiterals()) {
				Var variable = literal.getVariable();
				String label = labelProvider.apply(variable);
				if(!label.isEmpty())
					vars.put(label, variable);
			}
			
		return new CnfTask<Var>(cnf.toString(new MiniSatCnfPrinter<Var>(labelProvider)), vars);
	}
}
