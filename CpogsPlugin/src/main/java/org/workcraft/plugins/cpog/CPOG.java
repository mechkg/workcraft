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
			for(org.workcraft.plugins.cpog.scala.nodes.Variable var : eval(variables))
				if(eval(var.label()).equals(candidate))
					return false;
			return true;
		}
	});
	
	Function0<String> vertNameGen = new CheckedPrefixNameGen("v_", new Function<String, Boolean>(){
		@Override
		public Boolean apply(String candidate) {
			for(org.workcraft.plugins.cpog.scala.nodes.Vertex v : eval(vertices))
				if(eval(v.visualProperties().label()).equals(candidate))
					return false;
			return true;
		}
	});
	
	final ModifiableExpression<PVector<org.workcraft.plugins.cpog.scala.nodes.Variable>> variables;
	final ModifiableExpression<PVector<org.workcraft.plugins.cpog.scala.nodes.Vertex>> vertices;
	final ModifiableExpression<PVector<org.workcraft.plugins.cpog.scala.nodes.RhoClause>> rhoClauses;
	final ModifiableExpression<PVector<org.workcraft.plugins.cpog.scala.nodes.Arc>> arcs;

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
	}
	
	static <T> void add(ModifiableExpression<PVector<T>> vec, T item) {
		vec.setValue(eval(vec).plus(item));
	}

	public Arc connect(org.workcraft.plugins.cpog.scala.nodes.Vertex first, org.workcraft.plugins.cpog.scala.nodes.Vertex second) {
		org.workcraft.plugins.cpog.scala.nodes.Arc con = org.workcraft.plugins.cpog.scala.nodes.Arc.create(storage, first, second);
		add(arcs, con);
		return con;
	}

	public org.workcraft.plugins.cpog.scala.nodes.Vertex createVertex() {
		org.workcraft.plugins.cpog.scala.nodes.Vertex vertex = org.workcraft.plugins.cpog.scala.nodes.Vertex.create(storage);
		vertex.visualProperties().label().setValue(vertNameGen.apply());
		add(vertices, vertex);
		return vertex;
	}

	public org.workcraft.plugins.cpog.scala.nodes.RhoClause createRhoClause() {
		org.workcraft.plugins.cpog.scala.nodes.RhoClause rhoClause = org.workcraft.plugins.cpog.scala.nodes.RhoClause.create(storage);
		add(rhoClauses, rhoClause);
		return rhoClause;
	}

	public org.workcraft.plugins.cpog.scala.nodes.Variable createVariable() {
		String name = varNameGen.apply();
		org.workcraft.plugins.cpog.scala.nodes.Variable var = org.workcraft.plugins.cpog.scala.nodes.Variable.create(storage, storage.create(name));
		add(variables, var);
		return var;
	}

	public Expression<PVector<org.workcraft.plugins.cpog.scala.nodes.Node>> nodes() {
		final Expression<PVector<org.workcraft.plugins.cpog.scala.nodes.Component>> components = components();
		return new ExpressionBase<PVector<org.workcraft.plugins.cpog.scala.nodes.Node>>(){
			@Override
			protected PVector<org.workcraft.plugins.cpog.scala.nodes.Node> evaluate(EvaluationContext context) {
				return TreePVector.<org.workcraft.plugins.cpog.scala.nodes.Node>empty()
					.plusAll(context.resolve(components))
					.plusAll(context.resolve(arcs));
			}
		};
	}

	public Expression<PVector<org.workcraft.plugins.cpog.scala.nodes.Component>> components() {
		return new ExpressionBase<PVector<org.workcraft.plugins.cpog.scala.nodes.Component>>(){
			@Override
			protected PVector<org.workcraft.plugins.cpog.scala.nodes.Component> evaluate(EvaluationContext context) {
				return TreePVector.<org.workcraft.plugins.cpog.scala.nodes.Component>empty()
					.plusAll(context.resolve(variables))
					.plusAll(context.resolve(vertices))
					.plusAll(context.resolve(rhoClauses));
			}
		};
	}
}
