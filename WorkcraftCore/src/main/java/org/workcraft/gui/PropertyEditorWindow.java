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
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.core.IExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.util.Null;

@SuppressWarnings("serial")
public class PropertyEditorWindow extends JPanel {

	private PropertyEditorTable propertyTable;
	private JScrollPane scrollProperties;

	public PropertyEditorWindow (Framework framework) {
		propertyTable = new PropertyEditorTable(framework);

		scrollProperties = new JScrollPane();
		scrollProperties.setViewportView(propertyTable);

		setLayout(new BorderLayout(0,0));
		add(new DisabledPanel(), BorderLayout.CENTER);
		validate();
		
		GlobalCache.autoRefresh(new Expression<Null>() {
			@Override
			protected Null evaluate(EvaluationContext context) {
				Properties obj = context.resolve(prop);
				if(obj == null)
					clearObject();
				else
					setObject(obj);
				return null;
			}
			
		});
	}
	
	public final Variable<IExpression<? extends Properties>> propertyObject = new Variable<IExpression<? extends Properties>>(null);
	final IExpression<Properties> prop = Expressions.unfold(propertyObject);
	
	public Properties getObject () {
		return propertyTable.getObject();
	}

	public void setObject (Properties o) {
		removeAll();
		propertyTable.setObject(o);
		add(scrollProperties, BorderLayout.CENTER);
		validate();
		repaint();
	}

	public void clearObject () {
		if (propertyTable.getObject() != null) {
			removeAll();
			propertyTable.clearObject();
			add(new DisabledPanel(), BorderLayout.CENTER);
			validate();
			repaint();
		}
	}
}
