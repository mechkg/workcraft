package org.workcraft.plugins.cpog.optimisation.expressions.interfaces;

public interface FoldVisitor<Var, T> {
	T visitOne();
	T visitZero();
	T visitVariable(Var v);
	T visitAnd(T x, T y);
	T visitOr(T x, T y);
	T visitXor(T x, T y);
	T visitIff(T x, T y);
	T visitImply(T x, T y);
	T visitNot(T t1);
}
