package org.workcraft.plugins.cpog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.workcraft.plugins.cpog.scala.nodes.Variable;

public class Encoding
{
	private Map<Variable, VariableState> states = new HashMap<Variable, VariableState>();

	public Map<Variable, VariableState> getStates()
	{
		return Collections.unmodifiableMap(states);
	}

	public void setState(Variable variable, VariableState state)
	{
		states.put(variable, state);
	}
	
	public String toString()
	{
		String result = "";
		Set<Variable> sortedVariables = new TreeSet<Variable>(states.keySet()); 
		for(Variable var : sortedVariables) result += getState(var).toString();
		return result;
	}	

	public Encoding updateEncoding(String s)
	{
		Encoding result = new Encoding();
		int k = 0;
		Set<Variable> sortedVariables = new TreeSet<Variable>(states.keySet()); 
		for(Variable var : sortedVariables) result.states.put(var, VariableState.fromChar(s.charAt(k++)));
		return result;
	}

	public VariableState getState(Variable var)
	{
		VariableState res = states.get(var);
		if (res == null) res = VariableState.UNDEFINED;
		return res; 
	}

	public Encoding toggleState(Variable var)
	{
		Encoding result = new Encoding();
		result.states = new HashMap<Variable, VariableState>(states);
		result.setState(var, getState(var).toggle());
		return result;
	}	
}
