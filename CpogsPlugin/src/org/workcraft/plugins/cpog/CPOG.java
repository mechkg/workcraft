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

package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.util.Function;
import org.workcraft.util.Function0;

import pcollections.PVector;
import pcollections.TreePVector;

public class CPOG
{
	public final StorageManager storage;
	
	Function0<String> varNameGen = new CheckedPrefixNameGen("x_", new Function<String, Boolean>(){
		@Override
		public Boolean apply(String candidate) {
			for(Variable var : eval(variables))
				if(eval(var.label).equals(candidate))
					return false;
			return true;
		}
	});
	
	Function0<String> vertNameGen = new CheckedPrefixNameGen("v_", new Function<String, Boolean>(){
		@Override
		public Boolean apply(String candidate) {
			for(Vertex v : eval(vertices))
				if(eval(v.visualInfo.label).equals(candidate))
					return false;
			return true;
		}
	});
	
	final ModifiableExpression<PVector<Variable>> variables;
	final ModifiableExpression<PVector<Vertex>> vertices;
	final ModifiableExpression<PVector<RhoClause>> rhoClauses;
	final ModifiableExpression<PVector<Arc>> arcs;
	final ModifiableExpression<PVector<DynamicVariableConnection>> dynamicArcs;

	<T>ModifiableExpression<PVector<T>> createEmpty(StorageManager storage) {
		PVector<T> empty = TreePVector.empty();
		return storage.create(empty);
	}
	
	public CPOG(StorageManager storage) {
		this.storage = storage;
		variables = createEmpty(storage);
		vertices = createEmpty(storage);
		rhoClauses = createEmpty(storage);
		arcs = createEmpty(storage);
		dynamicArcs = createEmpty(storage);
	}
	
	static <T> void add(ModifiableExpression<PVector<T>> vec, T item) {
		vec.setValue(eval(vec).plus(item));
	}

	public Arc connect(Vertex first, Vertex second) {
		Arc con = new Arc(first, second, storage);
		add(arcs, con);
		return con;
	}

	public Vertex createVertex() {
		Vertex vertex = Vertex.make(storage);
		vertex.visualInfo.label.setValue(vertNameGen.apply());
		add(vertices, vertex);
		return vertex;
	}

	public RhoClause createRhoClause() {
		RhoClause rhoClause = RhoClause.make(storage);
		add(rhoClauses, rhoClause);
		return rhoClause;
	}

	public Variable createVariable() {
		String name = varNameGen.apply();
		Variable var = Variable.make(storage, storage.create(name));
		add(variables, var);
		return var;
	}

	public Expression<PVector<Node>> nodes() {
		return new ExpressionBase<PVector<Node>>(){

			@Override
			protected PVector<Node> evaluate(EvaluationContext context) {
				return TreePVector.<Node>empty()
					.plusAll(context.resolve(variables));
			}
			
		};
	}
}
