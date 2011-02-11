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

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.references.UniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class CPOG extends AbstractModel implements MathModel
{

	private final StorageManager storage;
	private final UniqueNameReferenceManager names;

	public CPOG(StorageManager storage)
	{
		this(null, null, storage);
	}

	public CPOG(Container root, StorageManager storage)
	{
		this(root, null, storage);
	}
	
	static class StartupParameters 
	{
		public StartupParameters(Container root, References refs, StorageManager storage) {
			this.storage = storage;
			this.root = root==null?new MathGroup(storage):root;
			this.names = new UniqueNameReferenceManager(this.root, refs, new Func<Node, String>(){
				@Override
				public String eval(Node arg) {
					return "badName";
				}
			});
			this.refMan = new HierarchySupervisor<ReferenceManager>(this.root, names);
		}
		StorageManager storage;
		Container root;
		Expression<? extends ReferenceManager> refMan;
		UniqueNameReferenceManager names;
	}

	public CPOG(StartupParameters p) 
	{
		super(createDefaultModelSpecification(p.root, p.refMan));
		this.names = p.names;
		this.storage = p.storage;
	}
	
	public CPOG(Container root, References refs, StorageManager storage)
	{
		this(new StartupParameters(root, refs, storage));
	}

	public String getName(Vertex vertex)
	{
		return names.getNodeReference(vertex);
	}

	public void setName(Vertex vertex, String name)
	{
		names.setName(vertex, name);
	}
	
	public Variable addVariable() 
	{
		Variable var = new Variable(storage);
		add(var);
		return var;
	}

	public Arc connect(Vertex first, Vertex second) throws InvalidConnectionException
	{
		Arc con = new Arc(first, second, storage);
		add(con);
		return con;
	}
	
	public DynamicVariableConnection connect(Vertex first, Variable second) throws InvalidConnectionException
	{
		DynamicVariableConnection con = new DynamicVariableConnection(first, second, storage);
		add(con);
		return con;
	}

	public Collection<Variable> getVariables() {
		return Hierarchy.getChildrenOfType(getRoot(), Variable.class);
	}

	public Vertex createVertex() {
		Vertex vertex = new Vertex(storage);
		add(vertex);
		return vertex;
	}

	public RhoClause createRhoClause() {
		RhoClause rhoClause = new RhoClause(storage);
		add(rhoClause);
		return rhoClause;
	}

	public Variable createVariable() {
		Variable variable = new Variable(storage);
		add(variable);
		return variable;
	}

}