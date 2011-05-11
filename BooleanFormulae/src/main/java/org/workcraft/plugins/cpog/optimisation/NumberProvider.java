package org.workcraft.plugins.cpog.optimisation;

import java.util.List;

interface NumberProvider<Var, Formula>
{
	public Formula generate(String varPrefix, int range);
	public BooleanFormula<Var> select(List<BooleanFormula<Var>> vars, Formula number);
	public BooleanFormula<Var> getConstraints();
	public BooleanFormula<Var> less(Formula a, Formula b);
}
