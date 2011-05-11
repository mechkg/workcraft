package org.workcraft.plugins.cpog.optimisation.dnf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.Literal;
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

public class DnfGenerator {
	public static <Var> Dnf<Var> generate(BooleanFormula<Var> formula)
	{
		return formula.accept(new BooleanVisitor<Var, Dnf<Var>>()
				{
					boolean negation = false;

					@Override
					public Dnf<Var> visit(And<Var> node) {
						Dnf<Var> left = node.getX().accept(this);
						Dnf<Var> right = node.getY().accept(this);
						return and(left, right);
					}

					private Dnf<Var> and(Dnf<Var> left,Dnf<Var> right) {
						return negation?addDnf(left,right):multiplyDnf(left, right);
					}

					private Dnf<Var> or(Dnf<Var> left,Dnf<Var> right) {
						return negation?multiplyDnf(left,right):addDnf(left, right);
					}

					@Override
					public Dnf<Var> visit(Iff<Var> node) {
						Dnf<Var> a = node.getX().accept(this);
						Dnf<Var> b = node.getY().accept(this);
						negation = !negation;
						Dnf<Var> na = node.getX().accept(this);
						Dnf<Var> nb = node.getY().accept(this);
						negation = !negation;
						return or(and(a,b), and(na, nb));
					}

					@Override
					public Dnf<Var> visit(Xor<Var> node) {
						Dnf<Var> a = node.getX().accept(this);
						Dnf<Var> b = node.getY().accept(this);
						negation = !negation;
						Dnf<Var> na = node.getX().accept(this);
						Dnf<Var> nb = node.getY().accept(this);
						negation = !negation;
						return or(and(a,nb), and(na, b));
					}

					private Dnf<Var> zero()
					{
						return negation ? new Dnf<Var>(new DnfClause()) : new Dnf<Var>(); 
					}
					
					@Override
					public Dnf<Var> visit(Zero<Var> node) {
						return zero();
					}

					@Override
					public Dnf<Var> visit(One<Var> node) {
						negation=!negation;
						Dnf<Var> result = zero();
						negation=!negation;
						return result;
					}

					@Override
					public Dnf<Var> visit(Not<Var> node) {
						negation = !negation;
						try{
						return node.getX().accept(this);
						}
						finally{negation=!negation;}
					}

					@Override
					public Dnf<Var> visit(Imply<Var> node) {
						negation=!negation;
						Dnf<Var> x = node.getX().accept(this);
						negation=!negation;
						Dnf<Var> y = node.getY().accept(this);
						return or(x,y);
					}

					@Override
					public Dnf<Var> visit(Variable<Var> variable) {
						return new Dnf<Var>(new DnfClause<Var>(new Literal<Var>(variable.variable(), negation)));
					}

					@Override
					public Dnf<Var> visit(Or<Var> node) {
						return or(node.getX().accept(this), node.getY().accept(this));
					}
					
				});
	}
	
	private static <T> boolean isFirstSmaller(HashSet<T> set1, HashSet<T> set2, boolean equalWins) {
		
		if (set2.containsAll(set1)) {
			if (set2.size()>set1.size()) return true;
			return equalWins;
		}
		
		return false;
	}
	
	
	// throws out all the repeated and absorbed clauses
	private static <Var> Dnf<Var> simplifyDnf(Dnf<Var> clauses) {
		Dnf<Var> result = new Dnf<Var>();
		
		Map <DnfClause<Var>, HashSet <Literal<Var>>> testClauses = new HashMap<DnfClause<Var>, HashSet <Literal<Var>>>();
		
		for (DnfClause<Var> clause: clauses.getClauses()) {
			
			if (clause.getLiterals().size()==0) return new Dnf<Var>(new DnfClause<Var>());
			
			HashSet<Literal<Var>> lset = new HashSet<Literal<Var>>();
			
			for (Literal<Var> lit: clause.getLiterals())
				lset.add(lit);
			
			testClauses.put(clause, lset);
		}
		
		for (DnfClause<Var> cleft: testClauses.keySet()) {
			for (DnfClause<Var> cright: testClauses.keySet()) {
				if (cleft==cright) continue;
				
				if (testClauses.get(cleft)==null) break;
				if (testClauses.get(cright)==null) continue;
				
				// left to right comparison
				if (isFirstSmaller(testClauses.get(cleft), testClauses.get(cright), true)) {
					testClauses.put(cright, null);
				} else if (isFirstSmaller(testClauses.get(cright), testClauses.get(cleft), false)) {
					// right to left comparison
					testClauses.put(cleft, null);
				}
			}
		}
		
		for (DnfClause<Var> cleft: testClauses.keySet())
			if (testClauses.get(cleft)!=null) 
				result.add(cleft);
		
		return result;
	}
	
	
	private static <Var> Dnf<Var> addDnf(Dnf<Var> left, Dnf<Var> right) {
		Dnf<Var> result = new Dnf<Var>();
		
		result.add(left);
		result.add(right);
		
		return simplifyDnf(result);
	}

	private static <Var> Dnf<Var> multiplyDnf(Dnf<Var> left, Dnf<Var> right) {
		Dnf<Var> result = new Dnf<Var>();
		for(DnfClause<Var> leftClause : left.getClauses()) {
			
			for(DnfClause<Var> rightClause : right.getClauses())
			{
				boolean foundSameLiteral;
				boolean clauseDiscarded = false;
				boolean sameNegation=false;
				
				DnfClause<Var> newClause = new DnfClause<Var>();
				
				newClause.add(leftClause.getLiterals());
				
				
				for(Literal<Var> rlit : rightClause.getLiterals()) {
					foundSameLiteral = false;
					
					for(Literal<Var> llit : leftClause.getLiterals()) {
						
						// TODO: work with 0 and 1 literals
						if (rlit.getVariable().equals(llit.getVariable())) {
							foundSameLiteral=true;
							sameNegation=llit.getNegation()==rlit.getNegation();
							break;
						}
					}

					if (!foundSameLiteral) newClause.add(rlit);
					else if (!sameNegation) {
						clauseDiscarded = true;
						break;
					}
				}
				
//				newClause.add(rightClause.getLiterals());
				if (!clauseDiscarded) result.add(newClause);
			}
		}
		
		return simplifyDnf(result);
	}
}
