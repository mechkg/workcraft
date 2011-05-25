package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Variable;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.cpog.optimisation.expressions.interfaces.FoldVisitor;
import org.workcraft.util.Function;
import org.workcraft.util.Memoise;

public class CachedVisitor {
	public static <Var, R> R visitEachNodeOnce(final FoldVisitor<Var, R> visitor, BooleanFormula<Var> formula) {
		return Fix.fix(new Function<Function<BooleanFormula<Var>, R>, Function<BooleanFormula<Var>, R>>(){

			@Override
			public Function<BooleanFormula<Var>, R> apply(Function<BooleanFormula<Var>, R> argument) {
				final Function<BooleanFormula<Var>, R> rec = Memoise.memoise(argument);
				return new Function<BooleanFormula<Var>, R>(){
					@Override
					public R apply(BooleanFormula<Var> argument) {
						return argument.accept(new BooleanVisitor<Var, R>() {

							@Override
							public R visit(And<Var> node) {
								R x = rec.apply(node.getX());
								R y = rec.apply(node.getY());
								return visitor.visitAnd(x, y);
							}

							@Override
							public R visit(Iff<Var> node) {
								R x = rec.apply(node.getX());
								R y = rec.apply(node.getY());
								return visitor.visitIff(x, y);
							}

							@Override
							public R visit(Xor<Var> node) {
								R x = rec.apply(node.getX());
								R y = rec.apply(node.getY());
								return visitor.visitXor(x, y);
							}

							@Override
							public R visit(Zero<Var> node) {
								return visitor.visitZero();
							}

							@Override
							public R visit(One<Var> node) {
								return visitor.visitOne();
							}

							@Override
							public R visit(Not<Var> node) {
								return visitor.visitNot(rec.apply(node.getX()));
							}

							@Override
							public R visit(Imply<Var> node) {
								return visitor.visitImply(rec.apply(node.getX()), rec.apply(node.getY()));
							}

							@Override
							public R visit(Variable<Var> variable) {
								return visitor.visitVariable(variable.variable());
							}

							@Override
							public R visit(Or<Var> node) {
								return visitor.visitOr(rec.apply(node.getX()), rec.apply(node.getY()));
							}
						});
					}
				};
			}
		}).apply(formula);
		
	}
}
