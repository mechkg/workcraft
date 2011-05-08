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

/*package org.workcraft.plugins.cpog;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;

public class VisualDynamicVariableConnection extends VisualConnection
{
	DynamicVariableConnection mathConnection;
	
	public VisualDynamicVariableConnection(DynamicVariableConnection mathConnection, StorageManager storage)
	{
		super(storage);
		this.mathConnection = mathConnection;
	}
	
	public VisualDynamicVariableConnection(DynamicVariableConnection mathConnection, VisualVertex first, VisualVariable second, StorageManager storage)
	{
		super(mathConnection, first, second, storage);
		this.mathConnection = mathConnection;
	}
	
	@Override
	public ExpressionBase<VisualConnectionProperties> properties() {
		return new ExpressionBase<VisualConnectionProperties>() {

			@Override
			protected VisualConnectionProperties evaluate(EvaluationContext context) {
				VisualConnectionProperties superProperties = context.resolve(VisualDynamicVariableConnection.super.properties());
				return new VisualConnectionProperties.Inheriting(superProperties){
					@Override
					public boolean hasArrow() {
						return true;
					}
				};
			}
		};
	}
}
*/
