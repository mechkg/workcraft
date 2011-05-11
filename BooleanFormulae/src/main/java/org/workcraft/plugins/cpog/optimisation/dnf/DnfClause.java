package org.workcraft.plugins.cpog.optimisation.dnf;

import java.util.List;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.Clause;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class DnfClause<Var> extends Clause<Literal<Var>> implements BooleanFormula<Var> {

	public DnfClause()
	{
	}
	
	public DnfClause(Literal<Var>... literals)
	{
		super(literals);
	}

	public DnfClause(List<Literal<Var>> literals) {
		super(literals);
	}
	
	@Override
	public <T> T accept(BooleanVisitor<Var, T> visitor) {
		return BooleanOperations.and(getLiterals()).accept(visitor);
	}
}
