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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.util.Hierarchy;

import pcollections.HashTreePSet;

/*public class VisualCPOG extends AbstractVisualModel
{
	private final class BooleanFormulaPropertyDescriptor implements PropertyDescriptor 
	{
		private final Node node;

		private BooleanFormulaPropertyDescriptor(Node node)
		{
			this.node = node;
		}

		@Override
		public Map<Object, String> getChoice() {
			return null;
		}

		@Override
		public String getName()
		{
			if (node instanceof VisualRhoClause) return "Function";
			return "Condition";
		}

		@Override
		public Class<?> getType() {
			return String.class;
		}

		@Override
		public Object getValue() throws InvocationTargetException
		{
			if (node instanceof VisualRhoClause) return FormulaToString.toString(eval(((VisualRhoClause)node).formula()));
			if (node instanceof VisualVertex) return FormulaToString.toString(eval(((VisualVertex)node).condition()));
			return FormulaToString.toString(eval(((VisualArc)node).condition()));
		}

		@Override
		public boolean isWritable() {
			return true;
		}

		@Override
		public void setValue(Object value) throws InvocationTargetException {
			try
			{
				ModifiableExpression<BooleanFormula> property = null;
				if (node instanceof VisualRhoClause) property = ((VisualRhoClause)node).formula();
				else
				if (node instanceof VisualArc) property = ((VisualArc)node).condition();
				else
				if (node instanceof VisualVertex) property = ((VisualVertex)node).condition();
				if(property != null)
					property.setValue(BooleanParser.parse((String)value, mathModel.getVariables()));
			} catch (ParseException e) {
				throw new InvocationTargetException(e);
			}
		}
	}

	private final CPOG mathModel;
	private final NameGenerator nameGenerator = new NameGenerator();

	public VisualCPOG(CPOG model, StorageManager storage)
	{
		this(model, null, storage);
	}

	public VisualCPOG(CPOG model, VisualGroup root, StorageManager storage)
	{
		super(model, root, storage);

		this.mathModel = model;

		if (root == null)
		{
			try
			{
				createDefaultFlatStructure();
			}
			catch (NodeCreationException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	private Collection<Node> getGroupableSelection()
	{
		HashSet<Node> result = new HashSet<Node>();
		
		for(Node node : getOrderedCurrentLevelSelection())
			if(node instanceof VisualVertex || node instanceof VisualVariable)
				result.add(node);
		
		return result;
	}
	
	@Override
	public void groupSelection()
	{
		Collection<Node> selected = getGroupableSelection();
		if (selected.size() < 1) return;

		VisualGroup group = new VisualScenario(storage);

		Container currentLevel = eval(currentLevel());
		
		currentLevel.add(group);

		currentLevel.reparent(selected, group);

		ArrayList<Node> connectionsToGroup = new ArrayList<Node>();

		for (VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class))
		{
			if (Hierarchy.isDescendant(connection.getFirst(), group) &&
				Hierarchy.isDescendant(connection.getSecond(), group))
			{
				connectionsToGroup.add(connection);
			}
		}

		currentLevel.reparent(connectionsToGroup, group);

		selection().setValue(HashTreePSet.<Node>singleton(group));
	}

	public Collection<VisualScenario> getGroups()
	{
		return Hierarchy.getChildrenOfType(getRoot(), VisualScenario.class);
	}

	public Collection<VisualVariable> getVariables()
	{
		return Hierarchy.getChildrenOfType(getRoot(), VisualVariable.class);
	}
	
	@Override
	public Properties getProperties(Node node) {
		Properties properties = super.getProperties(node);
		
		if(node instanceof VisualRhoClause || node instanceof VisualVertex || node instanceof VisualArc) 
			properties = Properties.Merge.add(properties, new BooleanFormulaPropertyDescriptor(node));
		
		return properties;
	}

	private void updateEncoding()
	{
		for(VisualScenario group : getGroups())
		{
			Encoding oldEncoding = eval(group.encoding());
			Encoding newEncoding = new Encoding();
		
			for(VisualVariable var : getVariables())
			{
				Variable mathVariable = var.getMathVariable();
				newEncoding.setState(mathVariable, oldEncoding.getState(mathVariable));
			}
		
			group.encoding().setValue(newEncoding);
		}
	}
	
	@Override
	public final void remove(Node node) {
		updateEncoding();
		super.remove(node);
	}
	
	@Override
	public final void remove(Collection<? extends Node> nodes) {
		super.remove(nodes);
		updateEncoding();
	}
}
*/
