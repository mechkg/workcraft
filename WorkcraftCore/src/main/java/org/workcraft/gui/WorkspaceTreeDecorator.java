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

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.gui.trees.ReactiveTreeDecorator;
import org.workcraft.gui.workspace.Path;
import org.workcraft.util.Function;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class WorkspaceTreeDecorator implements ReactiveTreeDecorator<Path<String>>
{
	private final Workspace workspace;

	public WorkspaceTreeDecorator(Workspace workspace)
	{
		this.workspace = workspace;
	}
	
	@Override
	public Expression<Icon> icon(Path<String> node) {
	
/*		try {
			return GUI.loadIconFromResource("images/place.png");
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			return null;
		}*/
		return Expressions.constant(null);
	}

	@Override
	public Expression<String> name(Path<String> node) {
		final WorkspaceEntry openFile = workspace.getOpenFile(node);
		final String name = node.isEmpty() ? "!Workspace" : node.getNode();
		return openFile == null ? Expressions.constant(name) : Expressions.fmap(new Function<Boolean, String>(){

			@Override
			public String apply(Boolean isChanged) {
				if(isChanged)
					return name + " *";
				else
					return name;
			}
			
		}, openFile.isChanged());
	}
}
