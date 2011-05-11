package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import static org.workcraft.util.Memoise.memoise;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Variable;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.Function;

public class VariableReplacer<V1, V2> extends BooleanVisitor<V1, BooleanFormula<V2>> {

	private final BooleanWorker worker;
	private final Function<V1, V2> f;
	private final Function<BooleanFormula<V1>, BooleanFormula<V2>> recurse;

	private VariableReplacer(Function<BooleanFormula<V1>, BooleanFormula<V2>> recurse, BooleanWorker worker, Function<V1, V2> f) {
		this.worker = worker;
		this.f = f;
		this.recurse = recurse;
	}
	
	public static <V1, V2> Function<BooleanFormula<V1>, BooleanFormula<V2>> uncachedReplacer(final BooleanWorker worker, final Function<V1, V2> f){
		return Fix.fix(new Fix.Endo<Function<BooleanFormula<V1>,BooleanFormula<V2>>>(){
			@Override
			public Function<BooleanFormula<V1>, BooleanFormula<V2>> apply(Function<BooleanFormula<V1>, BooleanFormula<V2>> recurse) {
				return new VariableReplacer<V1, V2>(recurse, worker, f);
			}
		});
	}
	
	public static <V1, V2> Function<BooleanFormula<V1>, BooleanFormula<V2>> cachedReplacer(final Function<V1, V2> f) {
		return cachedReplacer(new DumbBooleanWorker(), f);
	}
	
	public static <V1, V2> BooleanFormula<V2> replace(Function<V1, V2> f, BooleanFormula<V1> formula) {
		return cachedReplacer(f).apply(formula);
	}
	
	public static <V1, V2> Function<BooleanFormula<V1>, BooleanFormula<V2>> cachedReplacer(final BooleanWorker worker, final Function<V1, V2> f){
		return Fix.fix(new Fix.Endo<Function<BooleanFormula<V1>,BooleanFormula<V2>>>(){
			@Override
			public Function<BooleanFormula<V1>, BooleanFormula<V2>> apply(Function<BooleanFormula<V1>, BooleanFormula<V2>> recurse) {
				return memoise(new VariableReplacer<V1, V2>(recurse, worker, f));
			}
		});
	}
	
	
	@Override
	public BooleanFormula<V2> visit(And<V1> node) {
		return worker.and(recurse.apply(node.getX()), recurse.apply(node.getY()));
	}

	@Override
	public BooleanFormula<V2> visit(Iff<V1> node) {
		return worker.iff(recurse.apply(node.getX()), recurse.apply(node.getY()));
	}

	@Override
	public BooleanFormula<V2> visit(Xor<V1> node) {
		return worker.xor(recurse.apply(node.getX()), recurse.apply(node.getY()));
	}

	@Override
	public BooleanFormula<V2> visit(Zero<V1> node) {
		return worker.zero();
	}

	@Override
	public BooleanFormula<V2> visit(One<V1> node) {
		return worker.one();
	}

	@Override
	public BooleanFormula<V2> visit(Not<V1> node) {
		return worker.not(recurse.apply(node.getX()));
	}

	@Override
	public BooleanFormula<V2> visit(Imply<V1> node) {
		return worker.imply(recurse.apply(node.getX()), recurse.apply(node.getY()));
	}

	@Override
	public BooleanFormula<V2> visit(Variable<V1> variable) {
		return worker.var(f.apply(variable.variable()));
	}

	@Override
	public BooleanFormula<V2> visit(Or<V1> node) {
		return worker.or(recurse.apply(node.getX()), recurse.apply(node.getY()));
	}
}
