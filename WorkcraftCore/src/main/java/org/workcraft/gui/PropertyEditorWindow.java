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

package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.Framework;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;

import pcollections.PVector;
import pcollections.TreePVector;

@SuppressWarnings("serial")
public class PropertyEditorWindow extends JPanel {

	private PropertyEditorTable propertyTable;
	private JScrollPane scrollProperties;
	@SuppressWarnings("unused")//The field is used to keep reference to an otherwise garbage-collected refresher
	private final AutoRefreshExpression refresher;
	
	public PropertyEditorWindow (Framework framework) {
		propertyTable = new PropertyEditorTable(framework);

		scrollProperties = new JScrollPane();
		scrollProperties.setViewportView(propertyTable);

		setLayout(new BorderLayout(0,0));
		add(new DisabledPanel(), BorderLayout.CENTER);
		validate();
		
		refresher = new AutoRefreshExpression() {
			@Override
			protected void onEvaluate(EvaluationContext context) {
				PVector<EditableProperty> obj = context.resolve(prop);
				if(obj == null)
					clearObject();
				else
					setObject(obj);
			}
		};
	}
	
	public final Variable<Expression<? extends PVector<EditableProperty>>> propertyObject = new Variable<Expression<? extends PVector<EditableProperty>>>(Expressions.constant(TreePVector.<EditableProperty>empty()));
	final Expression<PVector<EditableProperty>> prop = Expressions.join(propertyObject);
	
	public void setObject (PVector<EditableProperty> o) {
		removeAll();
		propertyTable.setObject(o);
		add(scrollProperties, BorderLayout.CENTER);
		validate();
		repaint();
	}

	public void clearObject () {
		removeAll();
		propertyTable.clearObject();
		add(new DisabledPanel(), BorderLayout.CENTER);
		validate();
		repaint();
	}
}
